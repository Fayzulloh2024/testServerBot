

# Server Monitoring Bot

This bot is designed to monitor specified projects on a server by sending requests to a provided URL with a specified request body. Here’s how it works:

## Features

- **Send Request to Project URL**: The bot accepts a project URL and request body. It sends a request to this URL and monitors the response status.
- **Error Notification**: If the response status is anything other than 200, the bot will send a message to specified Telegram users. This message includes a button to take specific actions.
- **Server Start Button**: The notification includes a "Start Server" button. When this button is clicked, the bot runs the necessary commands to start the server (provided the commands have been previously configured).
  
## How It Works

1. **URL Monitoring**: The bot receives a URL and a request body, then sends a request to the URL.
2. **Response Check**: It checks the response. If the status code is not 200, it triggers a Telegram notification.
3. **Notification Message**: A message is sent to Telegram users with a "Start Server" button.
4. **Starting the Server**: When the "Start Server" button is pressed, the bot executes predefined commands to start the server, ensuring the commands are correctly configured beforehand.

## Requirements

- **Configured Commands**: To start the server remotely, commands need to be defined within the bot settings or configuration files.

## Usage

1. Set up the bot with your project’s URL and specify the necessary request body.
2. Define the server start commands within the bot’s configuration.
3. Monitor the bot for notifications on Telegram and use the "Start Server" button when needed.

