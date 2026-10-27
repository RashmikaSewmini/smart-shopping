
import {
    BrowserRouter,
    Routes,
    Route,
    Navigate,
    Outlet,
    NavLink,
    useNavigate,
    useLocation,
} from "react-router-dom";

import AIChatPage from "./pages/AIChatPage";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import HomePage from "./pages/HomePage";
import ShoppingPage from "./pages/ShoppingPage";
import CheckoutPage from "./pages/CheckoutPage";
import RecommendationsPage from "./pages/RecommendationsPage";
import PurchaseHistoryPage from "./pages/PurchaseHistoryPage";

import { getToken, logoutCustomer } from "./services/authApi";
import "./App.css";

function ProtectedRoute() {
    return getToken() ? <Outlet /> : <Navigate to="/login" replace />;
}

function AppNavigation() {
    const navigate = useNavigate();
    const location = useLocation();

    // Hide the navigation on login and registration pages.
    if (location.pathname === "/login" || location.pathname === "/register") {
        return null;
    }

    // Only show the navigation for authenticated users.
    if (!getToken()) {
        return null;
    }

    function handleLogout() {
        logoutCustomer();
        navigate("/login", { replace: true });
    }

    return (
        <header className="app-navigation">
            <button
                className="app-navigation-brand"
                onClick={() => navigate("/")}
                aria-label="Go to Home"
            >
                <span className="app-navigation-logo">✦</span>
                <span>Smart Shopping</span>
            </button>

            <nav className="app-navigation-links" aria-label="Main navigation">
                <NavLink
                    to="/"
                    end
                    className={({ isActive }) =>
                        `app-navigation-link ${isActive ? "active" : ""}`
                    }
                >
                    Home
                </NavLink>

                <NavLink
                    to="/recommendations"
                    className={({ isActive }) =>
                        `app-navigation-link ${isActive ? "active" : ""}`
                    }
                >
                    Recommendations
                </NavLink>

                <NavLink
                    to="/purchase-history"
                    className={({ isActive }) =>
                        `app-navigation-link ${isActive ? "active" : ""}`
                    }
                >
                    Purchase History
                </NavLink>

                <NavLink to="/ai-chat">AI Assistant</NavLink>

                <button
                    className="app-navigation-logout"
                    onClick={handleLogout}
                >
                    Logout
                </button>
            </nav>
        </header>
    );
}

function AppLayout() {
    return (
        <>
            <AppNavigation />
            <Outlet />
        </>
    );
}

export default function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/login" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />

                <Route element={<ProtectedRoute />}>
                    <Route element={<AppLayout />}>
                        <Route path="/" element={<HomePage />} />
                        <Route path="/shopping" element={<ShoppingPage />} />
                        <Route path="/checkout" element={<CheckoutPage />} />
                        <Route
                            path="/recommendations"
                            element={<RecommendationsPage />}
                        />
                        <Route
                            path="/purchase-history"
                            element={<PurchaseHistoryPage />}
                        />
                        <Route
                            path="/ai-chat"
                            element={
                                <ProtectedRoute>
                                    <AIChatPage />
                                </ProtectedRoute>
                            }
                        />
                    </Route>
                </Route>

                <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
        </BrowserRouter>
    );
}
