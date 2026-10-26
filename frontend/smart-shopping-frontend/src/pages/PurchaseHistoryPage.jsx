import { useCallback, useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getPurchaseHistory, getPurchaseSummary } from "../services/recommendationApi";
import { logoutCustomer } from "../services/authApi";
import "./PurchaseHistoryPage.css";

const money = (value) => new Intl.NumberFormat("en-LK", { style: "currency", currency: "LKR", minimumFractionDigits: 2 }).format(Number(value || 0));
const dateText = (value) => {
    if (!value) return "Date unavailable";
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? String(value) : date.toLocaleString("en-LK", { dateStyle: "medium", timeStyle: "short" });
};
const errorText = (error) => error.response?.data?.message || error.response?.data?.detail || error.message || "Could not load purchase history.";

export default function PurchaseHistoryPage() {
    const navigate = useNavigate();
    const [history, setHistory] = useState([]);
    const [summary, setSummary] = useState({});
    const [search, setSearch] = useState("");
    const [filter, setFilter] = useState("all");
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const loadHistory = useCallback(async () => {
        setLoading(true);
        setError("");
        try {
            const [historyData, summaryData] = await Promise.all([getPurchaseHistory(), getPurchaseSummary()]);
            setHistory(Array.isArray(historyData) ? historyData : []);
            setSummary(summaryData || {});
        } catch (err) {
            setError(errorText(err));
            if (err.response?.status === 401) {
                logoutCustomer();
                navigate("/login", { replace: true });
            }
        } finally {
            setLoading(false);
        }
    }, [navigate]);

    useEffect(() => { loadHistory(); }, [loadHistory]);

    const filteredHistory = useMemo(() => {
        const query = search.trim().toLowerCase();
        return history.filter((item) => {
            const isManual = item.productId == null;
            const matchesType = filter === "all" || (filter === "manual" && isManual) || (filter === "catalog" && !isManual);
            const matchesSearch = !query || [item.productName, item.purchaseId, item.productId == null ? "manual item" : "catalog item"].some((value) => String(value ?? "").toLowerCase().includes(query));
            return matchesType && matchesSearch;
        });
    }, [history, search, filter]);

    const groupedPurchases = useMemo(() => {
        const groups = new Map();
        for (const item of filteredHistory) {
            const key = item.purchaseId || `history-${item.id}`;
            if (!groups.has(key)) groups.set(key, { purchaseId: item.purchaseId || "Purchase", purchasedAt: item.purchasedAt, items: [] });
            const group = groups.get(key);
            group.items.push(item);
            if (item.purchasedAt && (!group.purchasedAt || new Date(item.purchasedAt) > new Date(group.purchasedAt))) group.purchasedAt = item.purchasedAt;
        }
        return [...groups.values()].sort((a, b) => new Date(b.purchasedAt || 0) - new Date(a.purchasedAt || 0));
    }, [filteredHistory]);

    const displayedTotal = filteredHistory.reduce((sum, item) => sum + Number(item.unitPrice || 0) * Number(item.quantity || 0), 0);

    return (
        <main className="purchase-history-page">

            <section className="history-hero">
                <div className="history-hero-copy">
                    <span className="history-eyebrow">YOUR SHOPPING JOURNEY</span>
                    <h1>Purchase history</h1>
                    <p>Every shop, every item, all in one place. Review what you bought and keep track of your spending.</p>
                </div>
                <div className="history-hero-art" aria-hidden="true"><span className="hero-bag">🛍️</span><span className="hero-spark spark-one">✦</span><span className="hero-spark spark-two">✧</span></div>
            </section>

            {error && <div className="history-error">{error}</div>}

            <section className="history-stats" aria-label="Purchase summary">
                <article className="history-stat-card"><span className="history-stat-icon green">↗</span><div><p>Total spent</p><strong>{money(summary.totalSpent ?? history.reduce((sum, item) => sum + Number(item.unitPrice || 0) * Number(item.quantity || 0), 0))}</strong><small>Across recorded purchases</small></div></article>
                <article className="history-stat-card"><span className="history-stat-icon orange">▤</span><div><p>Shopping trips</p><strong>{summary.purchaseCount ?? new Set(history.map((item) => item.purchaseId).filter(Boolean)).size}</strong><small>Completed checkouts recorded</small></div></article>
                <article className="history-stat-card"><span className="history-stat-icon purple">◫</span><div><p>Items purchased</p><strong>{summary.totalItemsPurchased ?? history.reduce((sum, item) => sum + Number(item.quantity || 0), 0)}</strong><small>Including manual items</small></div></article>
                <article className="history-stat-card"><span className="history-stat-icon blue">▦</span><div><p>History entries</p><strong>{history.length}</strong><small>Catalog and manual items</small></div></article>
            </section>

            <section className="history-list-panel">
                <div className="history-list-heading"><div><h2>All purchases</h2><p>Search by item name or checkout number.</p></div><span className="history-entry-count">{filteredHistory.length} entries</span></div>
                <div className="history-controls">
                    <label className="history-search"><span>⌕</span><input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search purchases..." aria-label="Search purchase history" /></label>
                    <div className="history-filters" role="group" aria-label="Filter purchase history">
                        {[ ["all", "All items"], ["catalog", "Catalog"], ["manual", "Manual items"] ].map(([value, label]) => <button key={value} className={filter === value ? "history-filter active" : "history-filter"} onClick={() => setFilter(value)}>{label}</button>)}
                    </div>
                </div>

                {loading ? <div className="history-empty"><span className="history-loader" />Loading your purchase history…</div> : groupedPurchases.length === 0 ? (
                    <div className="history-empty"><span className="empty-basket">🧺</span><h3>{history.length ? "No matching purchases" : "Your story starts here"}</h3><p>{history.length ? "Try another search or filter." : "Complete a checkout and your purchase history will appear here."}</p>{!history.length && <button className="history-primary-button" onClick={() => navigate("/shopping")}>Start shopping</button>}</div>
                ) : (
                    <div className="purchase-groups">
                        {groupedPurchases.map((group) => {
                            const groupTotal = group.items.reduce((sum, item) => sum + Number(item.unitPrice || 0) * Number(item.quantity || 0), 0);
                            return <article className="purchase-group" key={group.purchaseId}>
                                <header className="purchase-group-header"><div className="purchase-order-icon">▣</div><div className="purchase-order-meta"><strong>Checkout #{group.purchaseId}</strong><span>{dateText(group.purchasedAt)}</span></div><div className="purchase-order-total"><small>Checkout items total</small><strong>{money(groupTotal)}</strong></div></header>
                                <div className="purchase-items">
                                    {group.items.map((item, index) => <div className="purchase-history-item" key={item.id ?? `${group.purchaseId}-${item.productName}-${index}`}>
                                        <div className={item.productId == null ? "purchase-item-avatar manual" : "purchase-item-avatar"}>{item.productId == null ? "✎" : "▦"}</div>
                                        <div className="purchase-item-details"><strong>{item.productName || "Unnamed item"}</strong><span>{item.productId == null ? "Manually added item" : `Catalog product · ID ${item.productId}`}</span></div>
                                        <div className="purchase-item-quantity">× {item.quantity}</div>
                                        <div className="purchase-item-price"><strong>{money(Number(item.unitPrice || 0) * Number(item.quantity || 0))}</strong><span>{money(item.unitPrice)} each</span></div>
                                    </div>)}
                                </div>
                            </article>;
                        })}
                    </div>
                )}
                {!loading && filteredHistory.length > 0 && <footer className="history-list-footer"><span>Showing {filteredHistory.length} history entries</span><strong>Filtered total: {money(displayedTotal)}</strong></footer>}
            </section>
            <footer className="history-page-footer">Made for smarter shopping <span>✦</span></footer>
        </main>
    );
}
