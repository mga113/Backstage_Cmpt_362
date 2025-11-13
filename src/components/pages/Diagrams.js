import React from 'react';
import '../../App.css';
import './../Diagrams.css'; // <-- we’ll add this new CSS file next

export default function Diagrams() {
  return (
    <section id="diagrams" className="diagrams-section">
      <h1 className="diagrams-heading">MVVM Architecture</h1>
      <div className="diagrams-content">
        <img
          src="/images/MVVM.png"
          alt="System Architecture"
          className="diagram-img"
        />
      
      </div>
    </section>
  );
}
