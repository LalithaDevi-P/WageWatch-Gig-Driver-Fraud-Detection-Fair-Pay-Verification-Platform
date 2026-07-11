import React, { useState } from 'react';
import axios from 'axios';

const UploadForm = ({ currentUser }) => {
    const [formData, setFormData] = useState({
        platform: 'Swiggy', pickupArea: 'Gokulam', dropoffArea: 'Kuvempunagar',
        platformPay: '', platformDistanceKm: '', actualDrivenDistanceKm: '', extractedRatePerKm: 10.0, isRaining: false
    });
    const [loadingAI, setLoadingAI] = useState(false);

    const locationOptions = ["Gokulam", "Kuvempunagar", "JP Nagar", "Vijayanagar", "Metagalli Industrial Area", "Kyathamaranahalli", "Saraswathipuram", "Hebbal", "Bogadi", "Jayalakshmipuram"];

    const handleFileUpload = async (event) => {
        const file = event.target.files[0];
        if (!file) return;
        const uploadData = new FormData();
        uploadData.append("file", file);
        uploadData.append("isRaining", formData.isRaining);
        uploadData.append("platform", formData.platform);

        setLoadingAI(true);
        try {
            const response = await axios.post('http://localhost:8080/api/upload/scan', uploadData);
            let parsedData = typeof response.data === 'string' ? JSON.parse(response.data) : response.data;
            if (!parsedData.isAiExtractionFailed) {
                setFormData(prev => ({ ...prev, platformPay: parsedData.platformPay, platformDistanceKm: parsedData.platformDistanceKm }));
                alert("✅ Earnings scanned successfully! Please confirm the numbers.");
            } else {
                alert("⚠️ AI Scanner busy. Please enter manually.");
            }
        } catch (err) {
            alert("⚠️ AI Scanner unavailable. Please enter manually.");
        } finally {
            setLoadingAI(false);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const payload = {
                ...formData,
                platformPay: parseFloat(formData.platformPay),
                platformDistanceKm: parseFloat(formData.platformDistanceKm),
                actualDrivenDistanceKm: parseFloat(formData.actualDrivenDistanceKm)
            };

            // Sends the email to link the trip to the driver
            const response = await axios.post('http://localhost:8080/api/wage/upload', payload, {
                headers: { 'X-Driver-Email': currentUser }
            });

            // Re-added the detailed Fair Pay / Underpaid breakdown
            const savedTrip = response.data;
            if (savedTrip.isUnderpaid) {
                alert(`🚨 ALERT: YOU WERE UNDERPAID!\n\nPlatform paid: ₹${savedTrip.platformPay}\nFair Pay Should Be: ₹${savedTrip.calculatedFairPay}\n\nThey shorted you by ₹${savedTrip.underpaidAmount} (${savedTrip.underpaidPercentage}%).`);
            } else {
                alert(`✅ FAIR PAY VERIFIED!\n\nPlatform paid: ₹${savedTrip.platformPay}\nFair Pay Should Be: ₹${savedTrip.calculatedFairPay}`);
            }

            // THE FIX: Clear the form inputs instead of refreshing the entire page!
            setFormData(prev => ({
                ...prev,
                platformPay: '',
                platformDistanceKm: '',
                actualDrivenDistanceKm: ''
            }));

        } catch (err) {
            // THE FIX: Properly extract the smart error message from Spring Boot
            let errorMsg = "Failed to log trip.";
            if (err.response && err.response.data) {
                if (err.response.data.message) {
                    errorMsg = err.response.data.message;
                } else if (typeof err.response.data === 'string') {
                    try {
                        const parsed = JSON.parse(err.response.data);
                        if (parsed.message) errorMsg = parsed.message;
                    } catch (e) {
                        errorMsg = err.response.data;
                    }
                }
            }
            alert("❌ Validation Failed:\n" + errorMsg);
        }
    };

    const inputStyle = { width: '100%', padding: '12px', marginBottom: '16px', borderRadius: '8px', border: '1px solid #3d3d3d', backgroundColor: '#2d2d2d', color: '#fff', boxSizing: 'border-box' };

    return (
        <form onSubmit={handleSubmit} style={{ color: '#fff' }}>
            <h3 style={{ borderBottom: '1px solid #2ecc71', paddingBottom: '10px' }}>📍 Log Your Trip</h3>

            <label>Platform:</label>
            <select value={formData.platform} style={inputStyle} onChange={e => setFormData({...formData, platform: e.target.value})}>
                <option>Swiggy</option><option>Zomato</option><option>Blinkit</option>
            </select>

            <label style={{ display: 'flex', gap: '10px', marginBottom: '20px' }}>
                <input type="checkbox" checked={formData.isRaining} onChange={e => setFormData({...formData, isRaining: e.target.checked})}/> 🌧️ Raining?
            </label>

            <div style={{ background: '#262626', padding: '15px', borderRadius: '8px', marginBottom: '20px' }}>
                <h4>📸 Step 1: Upload Earnings</h4>
                <input type="file" onChange={handleFileUpload} accept="image/*" />
                {loadingAI && <p style={{ color: '#2ecc71' }}>Scanning...</p>}
            </div>

            <div style={{ background: '#262626', padding: '15px', borderRadius: '8px', marginBottom: '20px' }}>
                <h4>⚖️ Step 2: Verify</h4>
                <label>Pickup:</label>
                <select value={formData.pickupArea} style={inputStyle} onChange={e => setFormData({...formData, pickupArea: e.target.value})}>
                    {locationOptions.map(loc => <option key={loc}>{loc}</option>)}
                </select>
                <label>Dropoff:</label>
                <select value={formData.dropoffArea} style={inputStyle} onChange={e => setFormData({...formData, dropoffArea: e.target.value})}>
                    {locationOptions.map(loc => <option key={loc}>{loc}</option>)}
                </select>
                <label>Total Pay (₹):</label>
                <input type="number" step="0.01" value={formData.platformPay} style={inputStyle} onChange={e => setFormData({...formData, platformPay: e.target.value})} required />
                <div style={{ display: 'flex', gap: '10px' }}>
                    <div style={{ flex: 1 }}><label>Platform Claim:</label><input type="number" step="0.1" value={formData.platformDistanceKm} style={inputStyle} onChange={e => setFormData({...formData, platformDistanceKm: e.target.value})} required /></div>
                    <div style={{ flex: 1 }}><label>Actual Driven:</label><input type="number" step="0.1" value={formData.actualDrivenDistanceKm} style={inputStyle} onChange={e => setFormData({...formData, actualDrivenDistanceKm: e.target.value})} required /></div>
                </div>
            </div>
            <button type="submit" style={{ width: '100%', padding: '14px', background: '#2ecc71', border: 'none', borderRadius: '8px', fontWeight: 'bold', cursor: 'pointer' }}>Analyze Trip</button>
        </form>
    );
};
export default UploadForm;