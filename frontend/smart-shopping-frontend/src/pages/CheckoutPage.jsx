
import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';

import { getShoppingSession } from '../services/shoppingApi';
import { createCheckout } from '../services/checkoutApi';

export default function CheckoutPage() {
    const location = useLocation();
    const navigate = useNavigate();

    const sessionId = location.state?.sessionId;

    const [session, setSession] = useState(null);
    const [amountTendered, setAmountTendered] = useState('');
    const [receipt, setReceipt] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const money = (amount) =>
        `Rs. ${Number(amount ?? 0).toLocaleString('en-LK', {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2,
        })}`;

    useEffect(() => {
        if (!sessionId) return;

        let cancelled = false;

        async function loadSession() {
            setError('');

            try {
                const data = await getShoppingSession(sessionId);

                if (!cancelled) {
                    setSession(data);
                }
            } catch (err) {
                if (!cancelled) {
                    setError(
                        err.response?.data?.detail ||
                        err.response?.data?.message ||
                        'Could not load the shopping session.'
                    );
                }
            }
        }

        loadSession();

        return () => {
            cancelled = true;
        };
    }, [sessionId]);

    async function handleCheckout(event) {
        event.preventDefault();

        if (!session || !sessionId || amountTendered === '') {
            setError('Please enter the amount tendered.');
            return;
        }

        const paymentAmount = Number(amountTendered);
        const totalAmount = Number(session.total);

        if (
            !Number.isFinite(paymentAmount) ||
            paymentAmount < 0 ||
            !Number.isFinite(totalAmount)
        ) {
            setError('Please enter a valid payment amount.');
            return;
        }

        if (paymentAmount < totalAmount) {
            setError('The amount entered is less than the total.');
            return;
        }

        if (session.status !== 'ACTIVE') {
            setError('This shopping session is no longer active.');
            return;
        }

        if (!session.items?.length) {
            setError('Add at least one item before checkout.');
            return;
        }

        setLoading(true);
        setError('');

        try {
            // The backend expects a JSON object, not just a session ID.
            const checkoutData = {
                shoppingSessionId: Number(sessionId),
                amountTendered: paymentAmount,
            };

            const result = await createCheckout(checkoutData);

            setReceipt(result);
        } catch (err) {
            console.error("Checkout error:", err);
            console.error("Response status:", err.response?.status);
            console.error("Response data:", err.response?.data);

            setError(
                err.response?.data?.detail ||
                err.response?.data?.message ||
                err.message ||
                "Checkout failed."
            );
        } finally {
            setLoading(false);
        }
    }

    if (!sessionId) {
        return (
            <main className="page-container">
                <section className="empty-state">
                    <h1>No shopping session selected</h1>
                    <p>Return to shopping and add some products first.</p>

                    <Link to="/" className="button button-primary">
                        Back to shopping
                    </Link>
                </section>
            </main>
        );
    }

    if (receipt) {
        return (
            <main className="page-container receipt-container">
                <section className="receipt-card">
                    <div className="success-icon">✓</div>

                    <p className="eyebrow">PAYMENT COMPLETED</p>

                    <h1>Thank you for shopping!</h1>

                    <p className="subtitle">
                        Your checkout has been recorded successfully.
                    </p>

                    <div className="receipt-details">
                        <div>
                            <span>Checkout reference</span>
                            <strong>#{receipt.id}</strong>
                        </div>

                        <div>
                            <span>Shopping session</span>
                            <strong>#{receipt.shoppingSessionId}</strong>
                        </div>

                        <div>
                            <span>Total amount</span>
                            <strong>{money(receipt.totalAmount)}</strong>
                        </div>

                        <div>
                            <span>Amount tendered</span>
                            <strong>{money(receipt.amountTendered)}</strong>
                        </div>

                        <div className="change-row">
                            <span>Change to return</span>
                            <strong>{money(receipt.changeAmount)}</strong>
                        </div>
                    </div>

                    <button
                        type="button"
                        className="button button-primary button-full"
                        onClick={() => navigate('/')}
                    >
                        Start a new shopping trip
                    </button>
                </section>
            </main>
        );
    }

    return (
        <main className="page-container checkout-container">
            <div className="page-heading">
                <div>
                    <p className="eyebrow">FINAL STEP</p>
                    <h1>Checkout</h1>

                    <p className="subtitle">
                        Review your total and enter the amount you will pay.
                    </p>
                </div>

                <Link to="/" className="button button-outline">
                    Back to shopping
                </Link>
            </div>

            {error && (
                <div className="alert alert-error" role="alert">
                    {error}
                </div>
            )}

            {!session ? (
                <section className="panel loading-panel">
                    Loading shopping session...
                </section>
            ) : (
                <div className="checkout-grid">
                    <section className="panel">
                        <h2>Order summary</h2>

                        <p className="panel-description">
                            Shopping session #{session.id}
                        </p>

                        {session.items?.map((item) => (
                            <div className="summary-item" key={item.id}>
                                <div>
                                    <strong>{item.productName}</strong>

                                    <span>
                                        {item.quantity} × {money(item.unitPrice)}
                                    </span>
                                </div>

                                <strong>
                                    {money(item.lineTotal)}
                                </strong>
                            </div>
                        ))}

                        <div className="summary-total">
                            <span>Total to pay</span>
                            <strong>{money(session.total)}</strong>
                        </div>
                    </section>

                    <section className="panel payment-panel">
                        <h2>Cash payment</h2>

                        <p className="panel-description">
                            Enter the amount received from the customer.
                        </p>

                        {session.status !== 'ACTIVE' ? (
                            <div className="alert alert-error">
                                This session is no longer active and cannot
                                be checked out.
                            </div>
                        ) : !session.items?.length ? (
                            <div className="alert alert-error">
                                Add at least one item before checkout.
                            </div>
                        ) : (
                            <form onSubmit={handleCheckout}>
                                <label htmlFor="amountTendered">
                                    Amount tendered (Rs.)
                                </label>

                                <input
                                    id="amountTendered"
                                    name="amountTendered"
                                    type="number"
                                    min="0"
                                    step="0.01"
                                    value={amountTendered}
                                    onChange={(event) =>
                                        setAmountTendered(event.target.value)
                                    }
                                    placeholder="e.g. 2000.00"
                                    required
                                />

                                {amountTendered !== '' &&
                                    Number.isFinite(Number(amountTendered)) &&
                                    Number(amountTendered) >= Number(session.total) && (
                                        <div className="change-preview">
                                            <span>Estimated change</span>

                                            <strong>
                                                {money(
                                                    Number(amountTendered) -
                                                    Number(session.total)
                                                )}
                                            </strong>
                                        </div>
                                    )}

                                {amountTendered !== '' &&
                                    Number.isFinite(Number(amountTendered)) &&
                                    Number(amountTendered) < Number(session.total) && (
                                        <p className="validation-message">
                                            The amount entered is less than the total.
                                        </p>
                                    )}

                                <button
                                    type="submit"
                                    className="button button-primary button-full"
                                    disabled={
                                        loading ||
                                        amountTendered === '' ||
                                        !Number.isFinite(Number(amountTendered)) ||
                                        Number(amountTendered) < Number(session.total)
                                    }
                                >
                                    {loading
                                        ? 'Processing...'
                                        : 'Complete checkout'}
                                </button>
                            </form>
                        )}
                    </section>
                </div>
            )}
        </main>
    );
}