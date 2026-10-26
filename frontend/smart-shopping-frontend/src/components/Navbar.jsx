import { Link, useLocation } from 'react-router-dom';

export default function Navbar() {
    const location = useLocation();

    return (
        <header className="navbar">
            <Link to="/" className="brand">
                <span className="brand-icon">S</span>
                Smart Shopping
            </Link>

            <nav>
                <Link
                    to="/"
                    className={location.pathname === '/' ? 'nav-link active' : 'nav-link'}
                >
                    Shopping
                </Link>
            </nav>
        </header>
    );
}