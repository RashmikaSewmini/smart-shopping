import { useState } from "react";
import { Link } from "react-router-dom";
import { sendChatMessage } from "../services/aiChatApi";
import "./AIChatPage.css"

const suggestions = [
    "How can you help me find products?",
    "How can I compare product prices?",
    "Explain general precautions when using medicines.",
    "How can I find shops near me?",
];

export default function AIChatPage() {
    const [messages, setMessages] = useState([
        {
            role: "assistant",
            text: "Hi! I'm your Smart Shopping assistant. Ask me about shopping, products, or general medicine information.",
        },
    ]);
    const [input, setInput] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    async function sendMessage(text = input) {
        const message = text.trim();

        if (!message || loading) return;

        setInput("");
        setError("");
        setMessages((previous) => [
            ...previous,
            { role: "user", text: message },
        ]);
        setLoading(true);

        try {
            const result = await sendChatMessage(message);

            setMessages((previous) => [
                ...previous,
                { role: "assistant", text: result.reply },
            ]);
        } catch (err) {
            setError(err.message || "Something went wrong. Please try again.");
        } finally {
            setLoading(false);
        }
    }

    function handleSubmit(event) {
        event.preventDefault();
        sendMessage();
    }

    return (
        <main className="ai-chat-page">
            <header className="ai-chat-header">
                <div>
                    <p className="ai-chat-eyebrow">SMART SHOPPING</p>
                    <h1>AI Shopping Assistant</h1>
                    <p>Ask questions and get help with your shopping.</p>
                </div>

                <Link to="/" className="ai-chat-home">
                    Back to Home
                </Link>
            </header>

            <section
                className="ai-chat-panel"
                aria-label="Chat conversation"
            >
                <div className="ai-chat-messages" aria-live="polite">
                    {messages.map((message, index) => (
                        <div
                            key={`${index}-${message.role}`}
                            className={`ai-chat-message ${message.role}`}
                        >
              <span className="ai-chat-role">
                {message.role === "user" ? "You" : "AI Assistant"}
              </span>
                            <p>{message.text}</p>
                        </div>
                    ))}

                    {loading && (
                        <div className="ai-chat-loading" role="status">
                            Thinking...
                        </div>
                    )}
                </div>

                {messages.length === 1 && (
                    <div className="ai-chat-suggestions">
                        {suggestions.map((suggestion) => (
                            <button
                                type="button"
                                key={suggestion}
                                disabled={loading}
                                onClick={() => sendMessage(suggestion)}
                            >
                                {suggestion}
                            </button>
                        ))}
                    </div>
                )}

                {error && (
                    <p className="ai-chat-error" role="alert">
                        {error}
                    </p>
                )}

                <form className="ai-chat-form" onSubmit={handleSubmit}>
                    <input
                        value={input}
                        onChange={(event) => setInput(event.target.value)}
                        placeholder="Ask your question..."
                        aria-label="Your message"
                        maxLength={2000}
                        disabled={loading}
                    />
                    <button type="submit" disabled={loading || !input.trim()}>
                        Send
                    </button>
                </form>

                <p className="ai-chat-disclaimer">
                    AI responses can be inaccurate. For medicine-related decisions,
                    consult a qualified pharmacist or doctor. Do not use this chatbot
                    for emergencies.
                </p>
            </section>
        </main>
    );
}
