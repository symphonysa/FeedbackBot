# Feedback Bot
Leverage this bot framework to enable your teams to:
Automatically distribute feedback requests to relevant, selected contacts in a timely manner
Receive, consolidate, and log all client feedback in one secure chat room for all teams to read

Set the src/main/resources/sample-config.yml to look like this

    sessionAuthURL: https://your-pod.symphony.com/sessionauth
    keyAuthUrl: https://your-km.symphony.com:443/keyauth
    localKeystorePath: complete path to your jks keystore
    localKeystorePassword: keystore password
    botCertPath: complete path to your bot's p12 file
    botCertPassword: password
    botEmailAddress: bot.user@example.com
    agentAPIEndpoint: https://your-agent.symphony.com/agent
    podAPIEndpoint: https://your-pod.symphony.com/pod
    userEmailAddress: your-email@company.com
    mongoURL: url to your mongo database

Run BotMainApp.java
