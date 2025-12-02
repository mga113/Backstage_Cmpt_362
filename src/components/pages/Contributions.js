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
            '-Dual View System - List and Map view toggle with Google Maps integration',
            '-Event Management - Display saved events with "Interested" vs "Going" status tracking',
            'Advanced Filtering - Search, location, date range, and status filters',
            '-Swipe-to-Delete - Gesture-based deletion with undo functionality',
            '-Real-time Sync - Firebase Firestore listeners for instant updates across devices',
            '-Past Event Filtering - Automatically hides expired events',
            '-User Search - Real-time search by name/email with prefix matching',
            '-Friend Request System - Send, accept, decline with 4-state status tracking',
            '-Friends List - View all friends with profile access to see what events are they going to.',
            '-Scalable UI - "See all" pattern showing top 3 requests + full page'
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
            '-Friends-Only Messaging - Chat restricted to accepted friends from Friends tab, fetched via Firestore friends subcollection query',
            '-Real-time Chat - Instant bidirectional messaging with Firestore snapshot listeners on messages subcollection',
            '-Read Receipts - Per-user read status tracking, visual blue dot indicator for unread chats',
            '-Chat Preview - Display last message text, timestamp, and sender ID in chat list for context before opening conversation',
            '-Search Friends - Case-insensitive real-time search filtering by friend name within existing chat list',
            'Authentication - only allow unique usernames ',
            '-Auto-sorted List - Chats ordered by lastMessageTimestamp (descending) to show most recent conversations first'
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
            '-Event Discovery - Search concerts by artist, venue, or city using Ticketmaster Discovery API with 350ms debounce for optimized queries',
            '-Advanced Filtering - Genre chips, Material Date Picker for range selection, and 3 sort options',
            '-Location-Based Search - Automatic "Nearby" sorting using FusedLocationProvider for user coordinates and distance calculation from events',
            '-Event Interaction - Mark events as "Interested" or "Going" with mutual exclusivity, visual feedback, and instant status updates',
            '-Event Details - Dedicated activity showing full event info with external ticket purchase confirmation',
            '-Real-time Sync - Event statuses saved to Firestore users  collection, synced with MyInterests tab via snapshot listeners'
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
            '-Profile Management - Upload/change profile photo using Camera (with permission request) or Gallery picker',
            '-Image Storage - Upload to Firebase Storage, store download URL in Firestore with timestamp query parameter for cache invalidation',
            '-Account Security - Password reset via Firebase Auth sendPasswordResetEmail(), sign out with automatic FCM token cleanup',
            '-Location Services - One-time location capture on login/registration using FusedLocationProvider, reverse geocoding to city/state/country, stored in Firestore user document',
            '-Dynamic UI - RecyclerView with SettingsAdapter using sealed class pattern, different layouts for authenticated vs unauthenticated states'
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
