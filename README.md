[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-24ddc0f5d75046c5622901739e7c5dd533143b0c8e959d652212380cedb1ea36.svg)](https://classroom.github.com/a/I9qcx08w)

## SCREENSHOTS SHOWING THE MAIN SCREENS OF OUR APP
Inserted in screenshots_g03.zip ([a relative link](screenshots_g03.zip))

## FIRESTORE DATABASE RULES:
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {

    // This rule allows anyone with your Firestore database reference to view, edit,
    // and delete all data in your Firestore database. It is useful for getting
    // started, but it is configured to expire after 30 days because it
    // leaves your app open to attackers. At that time, all client
    // requests to your Firestore database will be denied.
    //
    // Make sure to write security rules for your app before that time, or else
    // all client requests to your Firestore database will be denied until you Update
    // your rules
    
    match /teams/{teamId} {
      allow read, write: if request.auth.uid in get(/databases/$(database)/documents/teams/$(teamId)).data.members;
    }
    
    match /comments/{commentId} {
      allow read, write: if get(/databases/$(database)/documents/comments/$(commentId)).data.taskId
      in get(/databases/$(database)/documents/people/$(request.auth.uid)).data.tasks;
    }
    
    match /task_replies/{replyId} {
      allow read, write: if get(/databases/$(database)/documents/tasks/$(get(/databases/$(database)/documents/comments/$(get(/databases/$(database)/documents/task_replies/$(replyId)).data.commentId)).data.taskId)).data.teamId in get(/databases/$(database)/documents/people/$(request.auth.uid)).data.teams;
    }
    
    match /tasks/{taskId} {
      allow read, write: if get(/databases/$(database)/documents/tasks/$(taskId)).data.teamId in get(/databases/$(database)/documents/people/$(request.auth.uid)).data.teams;
    }
    
    match /user_notifications/{notificationId} {
      allow read, write: if get(/databases/$(database)/documents/user_notifications/$(notificationId)).data.userId == request.auth.uid;
    }
    
    match /notifications/{notificationId} {
      allow read, write: if request.auth.uid in get(/databases/$(database)/documents/notifications/$(notificationId)).data.receivers;
    }
    
    match /team_participants/{tpId} {
      allow read, write: if get(/databases/$(database)/documents/team_participants/$(tpId)).data.teamId in get(/databases/$(database)/documents/people/$(request.auth.uid)).data.teams;
    }
    
    match /private_messages/{messageId} {
      allow read: if get(/databases/$(database)/documents/private_messages/$(messageId)).data.receiverId == request.auth.uid;
      allow read, write: if get(/databases/$(database)/documents/private_messages/$(messageId)).data.senderId == request.auth.uid;
    }
    
    match /team_messages/{messageId} {
      allow read: if get(/databases/$(database)/documents/team_messages/$(messageId)).data.teamId in get(/databases/$(database)/documents/people/$(request.auth.uid)).data.teams;
      allow read, write: if get(/databases/$(database)/documents/team_messages/$(messageId)).data.teamId in get(/databases/$(database)/documents/people/$(request.auth.uid)).data.teams && 
      get(/databases/$(database)/documents/team_messages/$(messageId)).data.senderId == request.auth.uid;
    }
    
    match /{document=**} {
      allow read, write: if request.time < timestamp.date(2024, 7, 15);
    }
    
  }
}

