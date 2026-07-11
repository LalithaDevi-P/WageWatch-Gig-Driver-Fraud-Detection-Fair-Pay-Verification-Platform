import React, { useEffect, useState } from 'react';
import axios from 'axios';

const Dashboard = ({ email }) => {
    const [trips, setTrips] = useState([]);

    useEffect(() => {
        // THE FIX: Corrected the API URL to match the TripController mapping
        axios.get(`http://localhost:8080/api/wage/history/${email}`)
            .then(res => setTrips(res.data))
            .catch(err => console.error("Error fetching trips:", err));
    }, [email]);

    // Calculate total stolen wages
    const totalLoss = trips.reduce((sum, trip) => sum + (trip.underpaidAmount || 0), 0);

    return (
        <div style={{ padding: '40px', color: 'white', width: '100%', overflowY: 'auto', backgroundColor: '#121212' }}>
            <h2 style={{ fontSize: '28px', marginBottom: '30px' }}>Welcome back, <span style={{ color: '#2ecc71' }}>{email}</span></h2>

            {/* Top Stat Cards */}
            <div style={{ display: 'flex', gap: '20px', marginBottom: '40px' }}>
                <div style={{ background: '#1a1a1a', padding: '25px', borderRadius: '12px', border: '1px solid #333', flex: 1 }}>
                    <h3 style={{ margin: '0 0 10px 0', color: '#aaa', fontSize: '16px' }}>Lifetime Trips Logged</h3>
                    <p style={{ fontSize: '32px', fontWeight: 'bold', margin: 0 }}>{trips.length}</p>
                </div>
                <div style={{ background: '#1a1a1a', padding: '25px', borderRadius: '12px', border: '1px solid #333', flex: 1 }}>
                    <h3 style={{ margin: '0 0 10px 0', color: '#aaa', fontSize: '16px' }}>Total Wages Stolen</h3>
                    <p style={{ fontSize: '32px', fontWeight: 'bold', margin: 0, color: '#e74c3c' }}>₹{totalLoss.toFixed(2)}</p>
                </div>
            </div>

            {/* History Table */}
            <div style={{ background: '#1a1a1a', borderRadius: '12px', border: '1px solid #333', overflow: 'hidden' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                    <thead style={{ backgroundColor: '#262626' }}>
                    <tr>
                        <th style={{ padding: '16px', color: '#aaa', fontWeight: 'normal' }}>Date</th>
                        <th style={{ padding: '16px', color: '#aaa', fontWeight: 'normal' }}>Platform</th>
                        <th style={{ padding: '16px', color: '#aaa', fontWeight: 'normal' }}>Route</th>
                        <th style={{ padding: '16px', color: '#aaa', fontWeight: 'normal' }}>Paid</th>
                        <th style={{ padding: '16px', color: '#aaa', fontWeight: 'normal' }}>Fair Pay</th>
                        <th style={{ padding: '16px', color: '#aaa', fontWeight: 'normal' }}>Status</th>
                    </tr>
                    </thead>
                    <tbody>
                    {trips.map((trip, index) => (
                        <tr key={index} style={{ borderBottom: '1px solid #333' }}>
                            <td style={{ padding: '16px' }}>{new Date(trip.createdAt).toLocaleDateString()}</td>
                            <td style={{ padding: '16px', fontWeight: 'bold' }}>{trip.platform}</td>
                            <td style={{ padding: '16px' }}>{trip.pickupArea} → {trip.dropoffArea}</td>
                            <td style={{ padding: '16px' }}>₹{trip.platformPay}</td>
                            <td style={{ padding: '16px' }}>₹{trip.calculatedFairPay}</td>
                            <td style={{ padding: '16px', fontWeight: 'bold', color: trip.isUnderpaid ? '#e74c3c' : '#2ecc71' }}>
                                {trip.isUnderpaid ? `Short by ₹${trip.underpaidAmount}` : 'Fair'}
                            </td>
                        </tr>
                    ))}
                    {trips.length === 0 && (
                        <tr>
                            <td colSpan="6" style={{ padding: '30px', textAlign: 'center', color: '#666' }}>No trips logged yet.</td>
                        </tr>
                    )}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default Dashboard;