import React, { useState } from 'react';
import axios from 'axios';

const ReceiptUploader = ({ onDataParsed }) => {
    const [loading, setLoading] = useState(false);

    const handleFileUpload = async (event) => {
        const file = event.target.files[0];
        if (!file) return;

        const formData = new FormData();
        formData.append("file", file);

        setLoading(true);
        try {
            // Sends the image to your Spring Boot AI controller
            const response = await axios.post('http://localhost:8080/api/ai/scan', formData);

            // The JSON from Gemini is parsed and passed back to your main form
            const parsedData = JSON.parse(response.data);
            onDataParsed(parsedData);
        } catch (error) {
            console.error("AI Scanning failed:", error);
            alert("Could not read receipt. Please try again.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="uploader-box">
            <input type="file" onChange={handleFileUpload} accept="image/*" />
            {loading && <p>AI is reading your receipt...</p>}
        </div>
    );
};

export default ReceiptUploader;