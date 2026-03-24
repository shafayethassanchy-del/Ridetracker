import React, { useState, useEffect, useRef } from 'react';
import { GoogleMap, useJsApiLoader, Polyline, Marker } from '@react-google-maps/api';
import { Bike, Play, Square, Fuel, History, Settings, User, Navigation, Clock, Gauge, TrendingUp } from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';

const containerStyle = {
  width: '100%',
  height: '100%'
};

const darkMapStyle = [
  { elementType: "geometry", stylers: [{ color: "#242f3e" }] },
  { elementType: "labels.text.stroke", stylers: [{ color: "#242f3e" }] },
  { elementType: "labels.text.fill", stylers: [{ color: "#746855" }] },
  {
    featureType: "administrative.locality",
    elementType: "labels.text.fill",
    stylers: [{ color: "#d59563" }],
  },
  {
    featureType: "poi",
    elementType: "labels.text.fill",
    stylers: [{ color: "#d59563" }],
  },
  {
    featureType: "road",
    elementType: "geometry",
    stylers: [{ color: "#38414e" }],
  },
  {
    featureType: "road",
    elementType: "geometry.stroke",
    stylers: [{ color: "#212a37" }],
  },
  {
    featureType: "road",
    elementType: "labels.text.fill",
    stylers: [{ color: "#9ca5b3" }],
  },
  {
    featureType: "water",
    elementType: "geometry",
    stylers: [{ color: "#17263c" }],
  },
];

export default function App() {
  const [isTracking, setIsTracking] = useState(false);
  const [distance, setDistance] = useState(0);
  const [time, setTime] = useState(0);
  const [path, setPath] = useState([]);
  const [currentPos, setCurrentPos] = useState({ lat: 23.8103, lng: 90.4125 });
  const [fuelConfig, setFuelConfig] = useState({ liters: 5, price: 110 });
  const [activeTab, setActiveTab] = useState('home');

  const timerRef = useRef(null);

  const { isLoaded } = useJsApiLoader({
    id: 'google-map-script',
    googleMapsApiKey: "YOUR_API_KEY_HERE" // In a real app, this would be an env var
  });

  useEffect(() => {
    if (isTracking) {
      timerRef.current = setInterval(() => {
        setTime(prev => prev + 1);
        
        // Simulate movement for demo
        const newPos = {
          lat: currentPos.lat + (Math.random() - 0.5) * 0.001,
          lng: currentPos.lng + (Math.random() - 0.5) * 0.001
        };
        setCurrentPos(newPos);
        setPath(prev => [...prev, newPos]);
        setDistance(prev => prev + 0.05); // Simulate 50m movement
      }, 1000);
    } else {
      clearInterval(timerRef.current);
    }
    return () => clearInterval(timerRef.current);
  }, [isTracking, currentPos]);

  const formatTime = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  const totalCost = fuelConfig.liters * fuelConfig.price;
  const costPerKm = distance > 0 ? totalCost / distance : 0;

  return (
    <div className="flex flex-col h-screen bg-[#121212] text-white font-sans overflow-hidden">
      {/* Header */}
      <header className="p-4 flex items-center justify-between border-b border-white/10 bg-[#1a1a1a]">
        <div className="flex items-center gap-3">
          <div className="bg-[#FFC107] p-2 rounded-lg">
            <Bike className="text-black w-6 h-6" />
          </div>
          <h1 className="text-xl font-bold tracking-tight">RideFuel <span className="text-[#FFC107]">Tracker</span></h1>
        </div>
        <button onClick={() => setActiveTab('profile')} className="w-10 h-10 rounded-full bg-white/10 flex items-center justify-center">
          <User className="w-5 h-5" />
        </button>
      </header>

      {/* Main Content */}
      <main className="flex-1 relative overflow-hidden">
        <AnimatePresence mode="wait">
          {activeTab === 'home' && (
            <motion.div 
              key="home"
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -20 }}
              className="h-full flex flex-col"
            >
              {/* Map View */}
              <div className="flex-1 relative bg-[#1e1e1e]">
                {isLoaded ? (
                  <GoogleMap
                    mapContainerStyle={containerStyle}
                    center={currentPos}
                    zoom={15}
                    options={{
                      styles: darkMapStyle,
                      disableDefaultUI: true,
                    }}
                  >
                    <Polyline
                      path={path}
                      options={{
                        strokeColor: "#FFC107",
                        strokeOpacity: 1,
                        strokeWeight: 5,
                      }}
                    />
                    <Marker position={currentPos} />
                  </GoogleMap>
                ) : (
                  <div className="w-full h-full flex items-center justify-center text-white/40">
                    Loading Map...
                  </div>
                )}

                {/* Live Stats Overlay */}
                <div className="absolute top-4 left-4 right-4 grid grid-cols-2 gap-3">
                  <div className="bg-[#1a1a1a]/90 backdrop-blur-md p-3 rounded-xl border border-white/10">
                    <div className="flex items-center gap-2 text-white/50 text-xs mb-1">
                      <Navigation className="w-3 h-3" />
                      Distance
                    </div>
                    <div className="text-xl font-bold text-[#FFC107]">{distance.toFixed(2)} <span className="text-xs text-white/50">km</span></div>
                  </div>
                  <div className="bg-[#1a1a1a]/90 backdrop-blur-md p-3 rounded-xl border border-white/10">
                    <div className="flex items-center gap-2 text-white/50 text-xs mb-1">
                      <Clock className="w-3 h-3" />
                      Time
                    </div>
                    <div className="text-xl font-bold">{formatTime(time)}</div>
                  </div>
                </div>
              </div>

              {/* Controls */}
              <div className="p-6 bg-[#1a1a1a] rounded-t-3xl shadow-2xl">
                <div className="flex justify-between items-center mb-6">
                  <div>
                    <p className="text-white/50 text-sm">Real-time Speed</p>
                    <div className="flex items-baseline gap-1">
                      <span className="text-3xl font-bold">42</span>
                      <span className="text-white/50 text-sm">km/h</span>
                    </div>
                  </div>
                  <div className="text-right">
                    <p className="text-white/50 text-sm">Est. Fuel Cost</p>
                    <div className="flex items-baseline gap-1 justify-end">
                      <span className="text-xl font-bold text-[#FFC107]">৳{costPerKm.toFixed(2)}</span>
                      <span className="text-white/50 text-sm">/km</span>
                    </div>
                  </div>
                </div>

                <button 
                  onClick={() => setIsTracking(!isTracking)}
                  className={`w-full py-4 rounded-2xl flex items-center justify-center gap-3 font-bold text-lg transition-all active:scale-95 ${
                    isTracking 
                    ? 'bg-red-500 text-white shadow-[0_0_20px_rgba(239,68,68,0.3)]' 
                    : 'bg-[#FFC107] text-black shadow-[0_0_20px_rgba(255,193,7,0.3)]'
                  }`}
                >
                  {isTracking ? (
                    <><Square className="fill-current" /> Stop Trip</>
                  ) : (
                    <><Play className="fill-current" /> Start Trip</>
                  )}
                </button>
              </div>
            </motion.div>
          )}

          {activeTab === 'fuel' && (
            <motion.div 
              key="fuel"
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -20 }}
              className="p-6 space-y-6"
            >
              <h2 className="text-2xl font-bold">Fuel Management</h2>
              <div className="space-y-4">
                <div className="bg-[#1e1e1e] p-4 rounded-2xl border border-white/10">
                  <label className="block text-white/50 text-sm mb-2">Fuel Amount (Liters)</label>
                  <input 
                    type="number" 
                    value={fuelConfig.liters}
                    onChange={(e) => setFuelConfig({...fuelConfig, liters: parseFloat(e.target.value)})}
                    className="w-full bg-transparent text-2xl font-bold outline-none text-[#FFC107]"
                  />
                </div>
                <div className="bg-[#1e1e1e] p-4 rounded-2xl border border-white/10">
                  <label className="block text-white/50 text-sm mb-2">Price per Liter (৳)</label>
                  <input 
                    type="number" 
                    value={fuelConfig.price}
                    onChange={(e) => setFuelConfig({...fuelConfig, price: parseFloat(e.target.value)})}
                    className="w-full bg-transparent text-2xl font-bold outline-none text-[#FFC107]"
                  />
                </div>
                <div className="bg-[#FFC107]/10 p-4 rounded-2xl border border-[#FFC107]/20">
                  <div className="flex justify-between items-center">
                    <span className="text-white/70">Total Fuel Cost</span>
                    <span className="text-2xl font-bold text-[#FFC107]">৳{totalCost}</span>
                  </div>
                </div>
              </div>
            </motion.div>
          )}

          {activeTab === 'history' && (
            <motion.div 
              key="history"
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -20 }}
              className="p-6 space-y-4"
            >
              <h2 className="text-2xl font-bold mb-4">Trip History</h2>
              {[1, 2, 3].map(i => (
                <div key={i} className="bg-[#1e1e1e] p-4 rounded-2xl border border-white/10 flex justify-between items-center">
                  <div>
                    <p className="font-bold">Trip to Office</p>
                    <p className="text-xs text-white/40">March 24, 2026 • 12.4 km</p>
                  </div>
                  <div className="text-right">
                    <p className="text-[#FFC107] font-bold">৳145</p>
                    <p className="text-[10px] text-white/40">৳11.6/km</p>
                  </div>
                </div>
              ))}
            </motion.div>
          )}
        </AnimatePresence>
      </main>

      {/* Bottom Navigation */}
      <nav className="bg-[#1a1a1a] border-t border-white/10 px-6 py-3 flex justify-between items-center">
        <NavButton active={activeTab === 'home'} onClick={() => setActiveTab('home')} icon={<Navigation />} label="Home" />
        <NavButton active={activeTab === 'fuel'} onClick={() => setActiveTab('fuel')} icon={<Fuel />} label="Fuel" />
        <NavButton active={activeTab === 'history'} onClick={() => setActiveTab('history')} icon={<History />} label="History" />
        <NavButton active={activeTab === 'settings'} onClick={() => setActiveTab('settings')} icon={<Settings />} label="Settings" />
      </nav>
    </div>
  );
}

function NavButton({ active, icon, label, onClick }) {
  return (
    <button 
      onClick={onClick}
      className={`flex flex-col items-center gap-1 transition-colors ${active ? 'text-[#FFC107]' : 'text-white/40'}`}
    >
      {React.cloneElement(icon, { size: 20, className: active ? 'fill-[#FFC107]/20' : '' })}
      <span className="text-[10px] font-medium uppercase tracking-wider">{label}</span>
    </button>
  );
}
