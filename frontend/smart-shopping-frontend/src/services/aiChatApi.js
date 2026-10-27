
const AI_CHAT_URL =
    import.meta.env.VITE_AI_CHAT_URL || "http://localhost:8086";

export async function sendChatMessage(message) {
    const response = await fetch(`${AI_CHAT_URL}/api/ai-chat`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({ message }),
    });

    if (!response.ok) {
        const error = await response.json().catch(() => ({}));
        throw new Error(error.message || "Unable to contact the AI assistant.");
    }

    return response.json();
}
