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
          href="https://github.com/mga113/Backstage_Cmpt_362"
        >
          Git Repo <i className="fab fa-github" />

        </Button>
 
        <Button
          className='btns'
          buttonStyle='btn--outline'
          buttonSize='btn--large'
          href="https://drive.google.com/file/d/1dMU_8gM8ZAYHUXgorVxciqKGa0eRhVfl/view?usp=sharing
"
        >
          ZIPPED CODE <i className='fas fa-download' />

        </Button>

        <Button
          className='btns'
          buttonStyle='btn--outline'
          buttonSize='btn--large'
          href="https://drive.google.com/file/d/1JduXVMaxffIgsvIrk8AXllGEHY-dA3-j/view?usp=sharing"
        >
          Download APK <i className='fas fa-download' />
        </Button>

        <Button
          className='btns'
          buttonStyle='btn--outline'
          buttonSize='btn--large'
          href="https://youtu.be/Zx8qAwGfUdA"
        >
          ORIGINAL PITCH <i className='far fa-play-circle' />
        </Button>

        <Button
          className='btns'
          buttonStyle='btn--outline'
          buttonSize='btn--large'
          href="https://www.youtube.com/watch?v=c5o3nRMdoOQ"
        >
          WATCH FINAL DEMO <i className='far fa-play-circle' />
        </Button>


      </div>
    </section>
  );
}

export default HeroSection;


