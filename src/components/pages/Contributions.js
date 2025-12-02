import React from 'react';
import '../../App.css';
import '../Contributions.css';

export default function Contributions() {
  const members = [
    {
      name: 'Darpandeep Kaur',
      role: 'Software Developer',
      versions: [
        {
          version: 'Show and Tell 1.0',
          date: 'Nov 6, 2025',
          tasks: [
            '- Implemented events marked as interested/ attending feature.',
            '- Implemented search interested events and see details feature.'
          ]
        },
        {
          version: 'Show and Tell 2.0',
          date: 'Nov 20, 2025',
          tasks: [
            '- Implemented Firestore for automatic data updates across devices.',
            '- Built a dual-layer filter system combining text search (title/location/date) with status chips.',
            '- Enabled direct status switching between "Going" and "Interested". ',
            '- Integrated swipe-to-delete functionality with "Undo" Snackbar support.',
            '- Implemented direct addition of events to the native Calendar.',
            '- Implemented sharing details of events via external apps.'
          ]
        },
        {
          version: 'Final Release',
          date: 'Dec 2, 2025',
          tasks: [
        
          ]
        }
      ]
    },
    {
      name: 'Mrinal Goshalia',
      role: 'Software Developer',
           versions: [
        {
          version: 'Show and Tell 1.0',
          date: 'Nov 6, 2025',
          tasks: [
            '- Created base chat implementation allowing users to message their friends and search through their contact list.',
          ]
        },
        {
          version: 'Show and Tell 2.0',
          date: 'Nov 20, 2025',
          tasks: [
            '- Implemented real-time chat messaging across app users using Firestore.',
            '- Developed a direct messaging UI with distinct sender and receiver message formatting.',
            '- Created intuitive login and registration interfaces.',
            '- Integrated full Firebase Authentication for secure user management.',
            '- Built a project website using ReactJS, including all essential information, and deployed it on Vercel.'

,
          ]
        },
        {
          version: 'Final Release',
          date: 'Dec 2, 2025',
          tasks: [
            'Coming soon!',
          ]
        }
      ]
    },
    {
      name: 'Noble Sekhon',
      role: 'Software Developer',
           versions: [
        {
          version: 'Show and Tell 1.0',
          date: 'Nov 6, 2025',
          tasks: [
            '- Implemented the basic UI for explore page.',
            '- Linked the Ticketmaster API to show the upcoming concerts on explore page.'
          ]
        },
        {
          version: 'Show and Tell 2.0',
          date: 'Nov 20, 2025',
          tasks: [
            '-Implemented live search that filters concerts by artist, event name, venue, and location as the user types.',
            '-Added genre filtering and date-range filtering, combining all filters into a dynamic query builder.',
            '- Connected Explore → My Interests by saving “Interested/Going” events to Firestore, enabling real-time syncing with MyInterestsFragment through snapshot listeners.'
          ]
        },
        {
          version: 'Final Release',
          date: 'Dec 2, 2025',
          tasks: [
            'Coming soon!',
          ]
        }
      ]
    },
    {
      name: 'Samanpreet Kaur',
      role: 'Software Developer',
           versions: [
        {
          version: 'Show and Tell 1.0',
          date: 'Nov 6, 2025',
          tasks: [
            '- Created base implementation of My Account page',
          ]
        },
        {
          version: 'Show and Tell 2.0',
          date: 'Nov 20, 2025',
          tasks: [
            '- Built complete MyAccount module with Firebase Authentication, Firestore user data, and full settings functionality.',
            '- Designed Ticketmaster-style UI with real-time Firestore sync using ViewModel + StateFlow architecture.'
          ]
        },
        {
          version: 'Final Release',
          date: 'Dec 2, 2025',
          tasks: [
            'Coming soon!',
          ]
        }
      ]
    }
  ];

  return (
    <section id="contributions" className="contributions">
      <h1 className="contributions-heading">Team Contributions</h1>
      <div className="contributions-container">
        {members.map((member, index) => (
          <div className="contribution-card" key={index}>
            <h2 className="member-name">{member.name}</h2>
            <h3 className="member-role">{member.role}</h3>

            <div className="member-timeline">
              {member.versions.map((v, i) => (
                <div className="timeline-entry" key={i}>
                  <div className="timeline-dot"></div>
                  <div className="timeline-content">
                    <h4 className="timeline-version">{v.version}</h4>
                    <p className="timeline-date">{v.date}</p>
                    <ul>
                      {v.tasks.map((task, j) => (
                        <li key={j}>{task}</li>
                      ))}
                    </ul>
                  </div>
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>
    </section>
  );
}
