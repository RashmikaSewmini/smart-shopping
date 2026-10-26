import { Link } from "react-router-dom";
import "./HomePage.css";

export default function HomePage() {
    const isLoggedIn = Boolean(
        localStorage.getItem("smartShoppingToken")
    );

    return (
        <div className="home-page">
            <main>
                <section className="home-hero">
                    <div className="hero-content">
            <span className="hero-tag">
              SMARTER SHOPPING, SIMPLER LIVING
            </span>

                        <h1>
                            Your shopping.
                            <br />
                            <span>Made smarter.</span>
                        </h1>

                        <p>
                            Scan products, track your shopping total in real time,
                            and check out with ease. A simpler way to shop.
                        </p>

                        <div className="hero-actions">
                            <Link to="/shopping" className="primary-button">
                                Start Shopping →
                            </Link>

                            {!isLoggedIn && (
                                <Link to="/register" className="secondary-button">
                                    Create Account
                                </Link>
                            )}
                        </div>

                        <div className="home-benefits">
                            <span>✓ Scan barcodes</span>
                            <span>✓ Track your total</span>
                            <span>✓ Calculate your change</span>
                        </div>
                    </div>

                    <div className="hero-visual">
                        <div className="shopping-illustration">
                            <div className="illustration-icon">🛍️</div>
                            <h2>Shopping made easy</h2>
                            <p>Scan. Shop. Pay. Done.</p>

                            <div className="illustration-item">
                                <span>🥛 Milk</span>
                                <span>Added to cart ✓</span>
                            </div>

                            <div className="illustration-item">
                                <span>🍞 Bread</span>
                                <span>Added to cart ✓</span>
                            </div>

                            <div className="illustration-total">
                                <span>Your shopping total</span>
                                <strong>Calculated automatically</strong>
                            </div>
                        </div>
                    </div>
                </section>

                <section className="home-features">
                    <div className="section-heading">
                        <span>WHY SMART SHOPPING?</span>
                        <h2>Everything you need for an easier shop</h2>
                    </div>

                    <div className="feature-grid">
                        <article className="feature-card">
                            <div className="feature-icon">📷</div>
                            <h3>Quick Barcode Scanning</h3>
                            <p>
                                Scan a product barcode with your device camera
                                to add it to your shopping list.
                            </p>
                        </article>

                        <article className="feature-card">
                            <div className="feature-icon">🧾</div>
                            <h3>Know Your Total</h3>
                            <p>
                                Keep track of item quantities and your running
                                shopping total.
                            </p>
                        </article>

                        <article className="feature-card">
                            <div className="feature-icon">💵</div>
                            <h3>Easy Checkout</h3>
                            <p>
                                Enter the amount you pay and view your checkout
                                receipt and change.
                            </p>
                        </article>
                    </div>
                </section>
            </main>

            <footer className="home-footer">
                <span>© {new Date().getFullYear()} Smart Shopping</span>
                <span>Shop smarter, every day.</span>
            </footer>
        </div>
    );
}