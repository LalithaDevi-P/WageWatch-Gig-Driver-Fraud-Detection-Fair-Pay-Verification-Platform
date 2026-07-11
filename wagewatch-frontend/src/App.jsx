import React, { useState } from 'react';
import AuthScreen from './AuthScreen';
import MapScreen from './MapScreen';
import UploadForm from './UploadForm';
import Dashboard from './Dashboard';
import './index.css'; // Assuming you have standard CSS reset here

const App = () => {
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [activeTab, setActiveTab] = useState('heatmap');
    const [currentUser, setCurrentUser] = useState('');

    if (!isAuthenticated) {
        return <AuthScreen onLoginSuccess={(email) => { setCurrentUser(email); setIsAuthenticated(true); }} />;
    }

    return (
        <div style={{ display: 'flex', flexDirection: 'column', height: '100vh', width: '100vw', backgroundColor: '#121212', margin: 0, overflow: 'hidden' }}>
            {/* Top Navigation */}
            <nav style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0 2rem', height: '65px', backgroundColor: '#1a1a1a', borderBottom: '1px solid #333' }}>
                <h2 style={{ color: '#2ecc71', margin: 0, fontSize: '1.4rem' }}>WageWatch</h2>
                <div style={{ display: 'flex', gap: '2rem' }}>
                    <button onClick={() => setActiveTab('heatmap')} style={{ background: 'none', border: 'none', color: activeTab === 'heatmap' ? '#2ecc71' : '#888', fontWeight: 'bold', fontSize: '16px', cursor: 'pointer', transition: 'color 0.2s' }}>
                        🗺️ City Heatmap
                    </button>
                    <button onClick={() => setActiveTab('dashboard')} style={{ background: 'none', border: 'none', color: activeTab === 'dashboard' ? '#2ecc71' : '#888', fontWeight: 'bold', fontSize: '16px', cursor: 'pointer', transition: 'color 0.2s' }}>
                        👤 My Dashboard
                    </button>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>
                    <span style={{ color: '#aaa', fontSize: '14px' }}>{currentUser}</span>
                    <button onClick={() => setIsAuthenticated(false)} style={{ padding: '8px 16px', background: '#e74c3c', color: 'white', border: 'none', borderRadius: '8px', cursor: 'pointer', fontWeight: 'bold' }}>
                        Logout
                    </button>
                </div>
            </nav>

            {/* Main Content Area */}
            <div style={{ flex: 1, display: 'flex', width: '100%', overflow: 'hidden' }}>
                {activeTab === 'heatmap' ? (
                    <>
                        <div style={{ width: '380px', backgroundColor: '#181818', padding: '20px', overflowY: 'auto', borderRight: '1px solid #333' }}>
                            <UploadForm currentUser={currentUser} />
                        </div>
                        <div style={{ flex: 1, position: 'relative' }}>
                            <MapScreen />
                        </div>
                    </>
                ) : (
                    <Dashboard email={currentUser} />
                )}
            </div>
        </div>
    );
};

export default App;