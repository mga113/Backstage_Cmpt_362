import React from 'react';
import '../App.css';
import { Button } from './Button';
import './HeroSection.css';

function HeroSection() {
  return (
    <section id='home' className='hero-container'>
      <video src='/videos/backstage_home_video.mp4' autoPlay loop muted />
      <h1>WHERE MUSIC MEETS CONNECTION</h1>
      <p>What are you waiting for?</p>
      <div className='hero-btns'>
        <Button
          className='btns'
          buttonStyle='btn--outline'
          buttonSize='btn--large'
        >
          ZIPPED CODE <i className='fas fa-download' />
        </Button>

        <Button
          className='btns'
          buttonStyle='btn--outline'
          buttonSize='btn--large'
          href="https://youtu.be/Zx8qAwGfUdA"
        >
          WATCH PITCH <i className='far fa-play-circle' />
        </Button>

        <Button
          className='btns'
          buttonStyle='btn--outline'
          buttonSize='btn--large'
          href="https://youtu.be/7225EoVwszQ"
        >
          WATCH FINAL DEMO <i className='far fa-play-circle' />
        </Button>

        <Button
          className='btns'
          buttonSize='btn--large'
        >
          Download APK <i className='fas fa-download' />
        </Button>
      </div>
    </section>
  );
}

export default HeroSection;