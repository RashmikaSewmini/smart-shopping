
import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getRecommendations } from "../services/recommendationApi";
import { logoutCustomer } from "../services/authApi";
import "./RecommendationsPage.css";

const money = (value) =>
    new Intl.NumberFormat("en-LK", {
        style: "currency",
        currency: "LKR",
        minimumFractionDigits: 2,
    }).format(Number(value ?? 0));

function getErrorMessage(error) {
    return (
        error.response?.data?.message ||
        error.response?.data?.detail ||
        error.message ||
        "We couldn't load recommendations right now."
    );
}

export default function RecommendationsPage() {
    const navigate = useNavigate();

    const [recommendations, setRecommendations] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const loadRecommendations = useCallback(async () => {
        setLoading(true);
        setError("");

        try {
            const data = await getRecommendations();
            setRecommendations(Array.isArray(data) ? data : []);
        } catch (err) {
            setError(getErrorMessage(err));

            if (err.response?.status === 401) {
                logoutCustomer();
                navigate("/login", { replace: true });
            }
        } finally {
            setLoading(false);
        }
    }, [navigate]);

    useEffect(() => {
        loadRecommendations();
    }, [loadRecommendations]);

    return (
        <main className="recommendations-page">
            <section className="recommendations-hero">
                <button
                    className="recommendations-home-button"
                    onClick={() => navigate("/")}
                >
                    <span aria-hidden="true">←</span>
                    Back to Home
                </button>

                <div className="recommendations-hero-copy">
                    <span className="recommendations-eyebrow">
                        PICKED FOR YOU
                    </span>

                    <h1>
                        Recommended
                        <br />
                        <span>for you.</span>
                    </h1>

                    <p>
                        Discover products selected from your shopping habits
                        and current offers.
                    </p>
                </div>

                <div
                    className="recommendations-hero-art"
                    aria-hidden="true"
                >
                    <span className="recommendations-orbit orbit-one" />
                    <span className="recommendations-orbit orbit-two" />
                    <span className="recommendations-bag">🛍️</span>
                    <span className="recommendations-star star-one">✦</span>
                    <span className="recommendations-star star-two">✧</span>
                </div>
            </section>

            <section className="recommended-products-section">
                <div className="recommended-section-heading">
                    <div>
                        <span className="recommendations-eyebrow">
                            YOUR NEXT FAVOURITES
                        </span>
                        <h2>Recommended products</h2>
                        <p>Ideas for your next shopping trip.</p>
                    </div>

                    <button
                        className="recommendations-refresh-button"
                        onClick={loadRecommendations}
                        disabled={loading}
                    >
                        <span aria-hidden="true">↻</span>
                        {loading ? "Refreshing..." : "Refresh"}
                    </button>
                </div>

                {loading ? (
                    <div className="recommendations-state">
                        <span className="recommendations-spinner" />
                        <h3>Finding your picks</h3>
                        <p>Loading recommendations for you...</p>
                    </div>
                ) : error ? (
                    <div className="recommendations-state recommendations-error">
                        <span className="recommendations-state-icon">!</span>
                        <h3>Couldn't load recommendations</h3>
                        <p>{error}</p>
                        <button onClick={loadRecommendations}>
                            Try again
                        </button>
                    </div>
                ) : recommendations.length === 0 ? (
                    <div className="recommendations-state">
                        <span className="recommendations-state-icon">✦</span>
                        <h3>Your recommendations are on the way</h3>
                        <p>
                            Complete a shopping trip or check back when
                            products have active offers. Your recommendations
                            will appear here.
                        </p>
                        <button onClick={() => navigate("/shopping")}>
                            Start shopping
                        </button>
                    </div>
                ) : (
                    <div className="recommended-products-grid">
                        {recommendations.map((product, index) => {
                            const productId =
                                product.productId ?? product.id ?? index;

                            const productName =
                                product.productName ||
                                product.name ||
                                "Product";

                            const regularPrice =
                                product.unitPrice ??
                                product.regularPrice ??
                                product.price ??
                                0;

                            const displayedPrice =
                                product.discountedPrice ?? regularPrice;

                            const savings = Number(product.savings ?? 0);
                            const hasDiscount = savings > 0;

                            const productEmojis = [
                                "🛒",
                                "🥛",
                                "🍎",
                                "🍞",
                                "🧴",
                                "🥫",
                            ];

                            return (
                                <article
                                    className="recommended-product-card"
                                    key={`${productId}-${index}`}
                                >
                                    <div
                                        className={`recommended-product-art product-art-${index % 6}`}
                                        aria-hidden="true"
                                    >
                                        <span className="product-art-spark">
                                            ✦
                                        </span>

                                        <span className="product-art-emoji">
                                            {productEmojis[index % 6]}
                                        </span>

                                        {hasDiscount && (
                                            <span className="product-offer-badge">
                                                Special offer
                                            </span>
                                        )}
                                    </div>

                                    <div className="recommended-product-details">
                                        <div className="recommended-product-title-row">
                                            <h3>{productName}</h3>
                                            <span className="recommended-product-number">
                                                {String(index + 1).padStart(2, "0")}
                                            </span>
                                        </div>

                                        {product.reason && (
                                            <p className="recommended-product-reason">
                                                {product.reason}
                                            </p>
                                        )}

                                        <div className="recommended-product-price-row">
                                            <strong>
                                                {money(displayedPrice)}
                                            </strong>

                                            {hasDiscount && (
                                                <span className="recommended-product-old-price">
                                                    {money(regularPrice)}
                                                </span>
                                            )}
                                        </div>

                                        {hasDiscount && (
                                            <p className="recommended-product-savings">
                                                Save {money(savings)}
                                            </p>
                                        )}
                                    </div>
                                </article>
                            );
                        })}
                    </div>
                )}
            </section>

            <footer className="recommendations-footer">
                Made for smarter shopping <span>✦</span>
            </footer>
        </main>
    );
}