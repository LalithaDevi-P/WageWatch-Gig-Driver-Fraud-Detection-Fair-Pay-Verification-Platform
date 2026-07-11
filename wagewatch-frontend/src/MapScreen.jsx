import React, { useEffect, useState } from 'react';
import { MapContainer, TileLayer, CircleMarker, Tooltip } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import axios from 'axios';

const MapScreen = () => {
    const [heatmapData, setHeatmapData] = useState([]);
    const [timeFilter, setTimeFilter] = useState('all');

    const exactLocations = {
        "Gokulam": [12.3218, 76.6272], "Vijayanagar": [12.3275, 76.6180], "Kuvempunagar": [12.2855, 76.6225],
        "Saraswathipuram": [12.3025, 76.6300], "JP Nagar": [12.2700, 76.6500], "Hebbal": [12.3535, 76.6025],
        "Metagalli Industrial Area": [12.3450, 76.6240], "Kyathamaranahalli": [12.3160, 76.6660],
        "Bogadi": [12.3000, 76.6000], "Jayalakshmipuram": [12.3125, 76.6355]
    };

    useEffect(() => {
        axios.get(`http://localhost:8080/api/wage/heatmap?filter=${timeFilter}`)
            .then(res => setHeatmapData(res.data))
            .catch(err => console.error(err));
    }, [timeFilter]);

    return (
        <div style={{ height: "100%", width: "100%", position: "relative" }}>
            <div style={{ position: "absolute", top: "20px", right: "20px", zIndex: 1000, backgroundColor: "#fff", padding: "10px", borderRadius: "8px" }}>
                <select value={timeFilter} onChange={(e) => setTimeFilter(e.target.value)} style={{ padding: "5px", cursor: "pointer" }}>
                    <option value="24h">Past 24 Hours</option><option value="7d">Past 7 Days</option>
                    <option value="30d">Past 30 Days</option><option value="all">All-Time</option>
                </select>
            </div>
            <MapContainer center={[12.3050, 76.6400]} zoom={13} style={{ height: "100%", width: "100%", zIndex: 1 }}>
                <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
                {heatmapData.map((data, index) => {
                    const lat = exactLocations[data.neighborhood] ? exactLocations[data.neighborhood][0] : data.latitude;
                    const lon = exactLocations[data.neighborhood] ? exactLocations[data.neighborhood][1] : data.longitude;
                    const tripCount = data.totalTrips || 0;

                    let markerColor = "#888";
                    let text = `Gathering (${tripCount}/7)`;
                    if (tripCount >= 7) {
                        markerColor = data.avgUnderpaymentPercentage > 0 ? "#e74c3c" : "#2ecc71";
                        text = data.avgUnderpaymentPercentage > 0 ? `⚠️ ${data.avgUnderpaymentPercentage.toFixed(2)}% Underpaid` : `✅ Fair`;
                    }
                    return (
                        <CircleMarker key={`${data.neighborhood}-${timeFilter}`} center={[lat, lon]} radius={15} pathOptions={{ fillColor: markerColor, color: "white", weight: 2, fillOpacity: 0.8 }}>
                            <Tooltip permanent direction="top"><b>{data.neighborhood}</b><br/>{text}</Tooltip>
                        </CircleMarker>
                    );
                })}
            </MapContainer>
        </div>
    );
};
export default MapScreen;