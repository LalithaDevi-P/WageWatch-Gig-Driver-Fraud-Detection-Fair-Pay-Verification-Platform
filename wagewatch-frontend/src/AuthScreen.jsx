import React, { useState } from 'react';
import axios from 'axios';

const AuthScreen = ({ onLoginSuccess }) => {
    const [isLogin, setIsLogin] = useState(true);
    const [formData, setFormData] = useState({ name: '', email: '', phoneNumber: '', password: '' });

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            if (isLogin) {
                // Hit the new login endpoint!
                const res = await axios.post('http://localhost:8080/api/auth/login', formData);
                onLoginSuccess(res.data.email);
            } else {
                // Registration endpoint
                await axios.post('http://localhost:8080/api/auth/register', formData);
                setIsLogin(true);
                alert("✅ Account created successfully! Please log in.");
            }
        } catch (err) {
            // Smart Error Handling
            if (err.response && err.response.data) {
                const errorData = err.response.data;

                // If it's a standard message (like "Email already in use")
                if (errorData.message) {
                    alert("❌ Error: " + errorData.message);
                }
                // If it's a DTO Validation Error (Spring Boot returns an object of field errors)
                else if (typeof errorData === 'object') {
                    const errorMessages = Object.values(errorData).join('\n• ');
                    alert("⚠️ Please fix the following:\n• " + errorMessages);
                } else {
                    alert("❌ Failed to process request.");
                }
            } else {
                alert("❌ Network Error: Make sure your Spring Boot server is running on port 8080!");
            }
        }
    };

    return (
        <div style={{
            minHeight: '100vh',
            width: '100%',
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            background: '#121212',
            margin: 0,
            boxSizing: 'border-box',
            overflowX: 'hidden', /* This kills the horizontal scroll gap */
            fontFamily: 'system-ui, sans-serif'
        }}>
            <div style={{ background: '#1a1a1a', padding: '40px', borderRadius: '16px', width: '90%', maxWidth: '400px', border: '1px solid #333', boxShadow: '0 10px 30px rgba(0,0,0,0.5)', textAlign: 'center' }}>
                <h1 style={{ color: '#2ecc71', margin: '0 0 30px' }}>WageWatch</h1>

                <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                    {!isLogin && <input type="text" placeholder="Full Name" onChange={e => setFormData({...formData, name: e.target.value})} required style={{ padding: '14px', borderRadius: '8px', border: '1px solid #444', background: '#262626', color: 'white', outline: 'none' }} />}
                    <input type="email" placeholder="Email Address" onChange={e => setFormData({...formData, email: e.target.value})} required style={{ padding: '14px', borderRadius: '8px', border: '1px solid #444', background: '#262626', color: 'white', outline: 'none' }} />
                    {!isLogin && <input type="text" placeholder="Phone Number (10 digits)" onChange={e => setFormData({...formData, phoneNumber: e.target.value})} required style={{ padding: '14px', borderRadius: '8px', border: '1px solid #444', background: '#262626', color: 'white', outline: 'none' }} />}
                    <input type="password" placeholder="Password" onChange={e => setFormData({...formData, password: e.target.value})} required style={{ padding: '14px', borderRadius: '8px', border: '1px solid #444', background: '#262626', color: 'white', outline: 'none' }} />

                    <button type="submit" style={{ padding: '14px', background: '#2ecc71', color: '#000', border: 'none', borderRadius: '8px', fontWeight: 'bold', fontSize: '16px', cursor: 'pointer', marginTop: '10px' }}>
                        {isLogin ? "Sign In" : "Create Account"}
                    </button>
                </form>

                <p style={{ marginTop: '20px', color: '#aaa', fontSize: '14px' }}>
                    {isLogin ? "Don't have an account? " : "Already have an account? "}
                    <span onClick={() => setIsLogin(!isLogin)} style={{ color: '#3498db', cursor: 'pointer', fontWeight: 'bold' }}>
                        {isLogin ? "Sign Up" : "Log In"}
                    </span>
                </p>
            </div>
        </div>
    );
};

export default AuthScreen;