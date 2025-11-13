import './Footer.css';
import { Link } from 'react-router-dom';

function Footer() {
  return (
    <div className='footer-container'>
   
      <section class='social-media'>
        <div class='social-media-wrap'>
          <div class='footer-logo'>
            <Link to='/' className='social-logo'>
              BACKSTAGE
            </Link>
          </div>
          <small class='website-rights'>BACKSTAGE © 2025</small>
        </div>
      </section>
    </div>
  );
}

export default Footer;
