import React, { useEffect, useState } from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import Navbar from './components/Navbar';
import './App.css';
import Diagrams from './components/pages/Diagrams';
import HeroSection from './components/HeroSection';
import Footer from './components/Footer';
import Contributions from './components/pages/Contributions';

function App() {
  const [activeSection, setActiveSection] = useState('home');

  useEffect(() => {
    const handleScroll = () => {
      const sections = document.querySelectorAll('section[id]');
      sections.forEach((section) => {
        const rect = section.getBoundingClientRect();
        if (rect.top <= 100 && rect.bottom >= 100) {
          setActiveSection(section.id);
        }
      });
    };
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  return (
    <Router>
      <Navbar activeSection={activeSection} />
      <section id="home">
        <HeroSection />
      </section>
      <Diagrams />
      <Contributions />
      <Footer />
    </Router>
  );
}

export default App;
