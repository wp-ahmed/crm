# CRM Web Application

This CRM (Customer Relationship Management) application is built using Spring Boot MVC, Thymeleaf, Hibernate, MySQL, and Java 17. The application provides a comprehensive solution for managing customer interactions, tasks, appointments, and communication. It also integrates with various Google services, including Google Drive, Gmail, and Google Calendar, to enhance productivity and collaboration.

## **Prerequisites**

Before installing the CRM application, ensure the following:

- Java 17 is installed on your machine.
- MySQL database is set up and running.
- Obtain valid MySQL connection details (URL, username, password).
- Obtain Google API credentials for integration with Google services (Drive, Gmail, Calendar).

## Installation

To install and run the CRM application, follow these steps:

1. Clone the repository from GitHub.
2. Configure the MySQL database connection details in the `application.properties` file:

```
spring.datasource.url=jdbc:mysql://localhost:3306/crm?createDatabaseIfNotExist=true
spring.datasource.username=YourUserName
spring.datasource.password=YourPassword
spring.jpa.hibernate.ddl-auto=none
spring.sql.init.mode=always
```

Replace `YourUserName` and `YourPassword` with your MySQL database credentials.

1. **Set up the necessary Google API credentials for Google integration:**
    - Go to the [Google Cloud Console](https://console.cloud.google.com/).
    - Create a new project or select an existing project.
    - Enable the necessary APIs for your project (e.g., Google Drive, Gmail, Calendar).
    - In the project dashboard, navigate to the **Credentials** section.
    - Click on **Create Credentials** and select **OAuth client ID**.
    - Configure the OAuth consent screen with the required information.
    - Choose the application type as **Web application**.
    - Add the authorized redirect URIs in the **Authorized redirect URIs** section. For example:
        - `http://localhost:8080/login/oauth2/code/google`
        - `http://localhost:8080/employee/settings/handle-granted-access`
        Replace `localhost:8080` with the base URL of your CRM application.
    - Complete the setup and note down the **Client ID** and **Client Secret**.
2. **Modify the Google API scopes for accessing Google services**:
    
    While setting up the Google API credentials, you need to add the required scopes to define the level of access the application has to your Google account. The required scopes depend on the specific features you want to use. Here are the scopes for common Google services:
    
    - Google Drive: `https://www.googleapis.com/auth/drive`
    - Gmail: `https://www.googleapis.com/auth/gmail.readonly`
    - Google Calendar: `https://www.googleapis.com/auth/calendar`
        
        During the setup of your Google credentials, find the section to add the API scopes and include the scopes relevant to the features you intend to use.
        
        ![non-sensitive scopes](https://github.com/wp-ahmed/crm/assets/54330098/f1bc7026-591a-4d40-affa-e038e29591b2)

        ![sensitive scopes](https://github.com/wp-ahmed/crm/assets/54330098/14d82922-0904-45d0-9874-da18c90fb352)

        ![restricted scopes](https://github.com/wp-ahmed/crm/assets/54330098/b76a5cf8-c342-42e9-9848-6d0844f83575)

        
3. **Configure the redirect URI for the Google authentication flow:**

```
spring.security.oauth2.client.registration.google.redirect-uri={baseUrl}/login/oauth2/code/{registrationId}

```

1. Customize the authorization and authentication URLs for the application if needed:

```
spring.security.oauth2.client.provider.google.authorization-uri=https://accounts.google.com/o/oauth2/auth
spring.security.oauth2.client.provider.google.token-uri=https://accounts.google.com/o/oauth2/token

```

1. Build the application using Maven:

```bash
mvn clean install

```

1. Run the application:

```bash
mvn spring-boot:run

```

1. Access the CRM application in your web browser at `http://localhost:8080`.

## Features

### User Authentication and Authorization

- Users can log in using their regular credentials or choose to log in using their Google accounts.
- Google login allows users to grant access to Google Drive, Gmail, and Google Calendar.

### Google Drive Integration

- Users can create, delete, and share files and folders with colleagues directly from the CRM application.
- Integration with Google Drive enables seamless collaboration and document management.

### Google Calendar Integration

- Integrated with FullCalendar JS library, users can easily manage their calendar, create, edit, and delete meetings.
- Automated email notifications are sent to attendees when meetings are scheduled or modified.

### Google Gmail Integration

- Users can send emails, save drafts, and manage their inbox, sent items, drafts, and trash directly within the CRM application.
- Gmail integration streamlines communication and enables efficient email management.

### User Roles and Permissions

- The application supports different roles, including Manager, Employee, Sales, and Customers.
- Each role has specific access and permissions tailored to their responsibilities.

### Manager Role

- Managers have access to all features and functionalities in the CRM application.
- They can create new users, assign specific roles to users, define new roles, and manage access to different pages for employees.
- Managers can assign tickets and leads to employees for efficient task allocation.

### Employee Role

- Employees have access to their assigned tickets, leads, contracts, and task history.
- They can manage their customers and create new tickets.
- Employees receive email notifications for newly assigned tasks (configurable in user settings).

### Customer Role

- Customers have access to their tickets, leads, and contracts.
- They receive email notifications for any changes to their tickets, leads, or contracts.
- Customers can manage their notification preferences in their settings.

### Leads Management

- Users can create, update, delete, and view leads.
- Integration with Google Drive allows automatic saving of lead attachments.
- Integration with Google Calendar enables scheduling meetings with customers.

### Tickets Management

- Users can create, update, delete, and view tickets.
- Integration with Google Drive allows automatic saving of ticket attachments.
- Integration with Google Calendar enables scheduling meetings related to tickets.

### Contracts Management

- Users can create, update, delete, and view contracts.
- Contracts can include details such as amount, start and end dates, description, and attachments.
- Integration with Google Drive allows uploading and sharing contracts with customers.

### Email Templates and Campaigns

- Users can create personalized email templates using the Unlayer library's drag-and-drop functionality.
- Email campaigns can be created using the predefined templates.

### User Settings

- Users can configure email settings and Google service access from their settings page.
- Email settings allow employees to enable or disable the automatic sending of emails to customers using predefined email templates when tickets, leads, or other objects are updated.
- Google settings allow users to manage access to Google services, enabling or disabling integration with Google Drive, Gmail, and Google Calendar.

## Contributing

Contributions to the CRM Web Application are welcome! If you spot any bugs or would like to propose new features, please open an issue or submit a pull request.

## License

This project is licensed under the MIT License.
