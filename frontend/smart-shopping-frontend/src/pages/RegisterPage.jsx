
import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { registerCustomer } from "../services/authApi";

export default function RegisterPage() {
    const navigate = useNavigate();

    const [form, setForm] = useState({
        name: "",
        email: "",
        password: "",
    });

    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    function handleChange(event) {
        setForm({
            ...form,
            [event.target.name]: event.target.value,
        });
    }

    async function handleSubmit(event) {
        event.preventDefault();
        setError("");
        setLoading(true);

        try {
            await registerCustomer(form);
            navigate("/login", {
                state: { message: "Registration successful. Please log in." },
            });
        } catch (err) {
            setError(
                err.response?.data?.message ||
                err.response?.data?.detail ||
                err.message ||
                "Registration failed."
            );
        } finally {
            setLoading(false);
        }
    }

    return (
        <main className="auth-page">
            <form className="auth-card" onSubmit={handleSubmit}>
                <h1>Create your account</h1>
                <p>Join Smart Shopping today.</p>

                {error && <div className="error-message">{error}</div>}

                <label htmlFor="name">Full name</label>
                <input
                    id="name"
                    name="name"
                    value={form.name}
                    onChange={handleChange}
                    required
                    autoComplete="name"
                />

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
                    minLength={8}
                    autoComplete="new-password"
                />

                <button type="submit" disabled={loading}>
                    {loading ? "Creating account..." : "Register"}
                </button>

                <p>
                    Already registered? <Link to="/login">Log in</Link>
                </p>
            </form>
        </main>
    );
}
