import React, { useState, useEffect } from 'react';
import { Link as ScrollLink } from 'react-scroll';
import './Navbar.css';

function Navbar({ activeSection }) {
  const [click, setClick] = useState(false);
  const [button, setButton] = useState(true);

  const handleClick = () => setClick(!click);
  const closeMobileMenu = () => setClick(false);

  const showButton = () => {
    if (window.innerWidth <= 960) {
      setButton(false);
    } else {
      setButton(true);
    }
  };

  useEffect(() => {
    showButton();
    window.addEventListener('resize', showButton);
    return () => window.removeEventListener('resize', showButton);
  }, []);

  return (
    <nav className='navbar'>
      <div className='navbar-container'>
        <ScrollLink
          to='home'
          smooth={true}
          duration={500}
          className='navbar-logo'
          onClick={closeMobileMenu}
        >
          BACKSTAGE
        </ScrollLink>

        <div className='menu-icon' onClick={handleClick}>
          <i className={click ? 'fas fa-times' : 'fas fa-bars'} />
        </div>

        <ul className={click ? 'nav-menu active' : 'nav-menu'}>
          <li className='nav-item'>
            <ScrollLink
              to='home'
              smooth={true}
              duration={500}
              offset={-80}
              spy={true}
              className={`nav-links ${activeSection === 'home' ? 'active' : ''}`}
              onClick={closeMobileMenu}
            >
              HOME
            </ScrollLink>
          </li>

          <li className='nav-item'>
            <ScrollLink
              to='diagrams'
              smooth={true}
              duration={500}
              offset={-80}
              spy={true}
              className={`nav-links ${activeSection === 'diagrams' ? 'active' : ''}`}
              onClick={closeMobileMenu}
            >
              DIAGRAMS
            </ScrollLink>
          </li>

          <li className='nav-item'>
            <ScrollLink
              to='contributions'
              smooth={true}
              duration={500}
              offset={-80}
              spy={true}
              className={`nav-links ${activeSection === 'contributions' ? 'active' : ''}`}
              onClick={closeMobileMenu}
            >
              CONTRIBUTIONS
            </ScrollLink>
          </li>
        </ul>
      </div>
    </nav>
  );
}

export default Navbar;
