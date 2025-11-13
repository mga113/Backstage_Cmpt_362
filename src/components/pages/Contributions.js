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
            'Coming soon!',
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
            'Coming soon!',
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
            'Coming soon!',
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
            'Coming soon!',
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
