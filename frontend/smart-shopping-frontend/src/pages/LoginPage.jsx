
import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { loginCustomer } from "../services/authApi";

export default function LoginPage() {
    const navigate = useNavigate();
    const location = useLocation();

    const [form, setForm] = useState({
        email: "",
        password: "",
    });

    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    function handleChange(event) {
        const { name, value } = event.target;

        setForm((previousForm) => ({
            ...previousForm,
            [name]: value,
        }));
    }

    async function handleSubmit(event) {
        event.preventDefault();
        setError("");
        setLoading(true);

        try {
            await loginCustomer(form);

            // Navigate to the Home Page after successful login.
            navigate("/", { replace: true });
        } catch (err) {
            setError(
                err.response?.data?.message ||
                err.response?.data?.detail ||
                err.message ||
                "Login failed."
            );
        } finally {
            setLoading(false);
        }
    }

    return (
        <main className="auth-page">
            <form className="auth-card" onSubmit={handleSubmit}>
                <h1>Welcome back</h1>
                <p>Log in to your Smart Shopping account.</p>

                {location.state?.message && (
                    <div className="success-message">
                        {location.state.message}
                    </div>
                )}

                {error && (
                    <div className="error-message">
                        {error}
                    </div>
                )}

                <label htmlFor="email">Email address</label>
                <input
                    id="email"
                    name="email"
                    type="email"
                    value={form.email}
                    onChange={handleChange}
                    required
                    autoComplete="email"
                />

                <label htmlFor="password">Password</label>
                <input
                    id="password"
                    name="password"
                    type="password"
                    value={form.password}
                    onChange={handleChange}
                    required
                    autoComplete="current-password"
                />

                <button type="submit" disabled={loading}>
                    {loading ? "Logging in..." : "Log in"}
                </button>

                <p>
                    Don't have an account?{" "}
                    <Link to="/register">Register</Link>
                </p>
            </form>
        </main>
    );
}
