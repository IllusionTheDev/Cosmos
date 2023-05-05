<h1 align="center"><img height="35" src="https://emoji.gg/assets/emoji/7333-parrotdance.gif"> Cosmos</h1>
<div align="center">

![GitHub Repo stars](https://img.shields.io/github/stars/IllusionTheDev/Cosmos?style=for-the-badge) 
![GitHub watchers](https://img.shields.io/github/watchers/IllusionTheDev/Cosmos?style=for-the-badge) 
![GitHub issues](https://img.shields.io/github/issues/IllusionTheDev/Cosmos?style=for-the-badge)

</div>

#### This project consists of a World Template Management System

This project is in ALPHA, which means that there is some core functionality. Do not use this in a production server (yet).

------------

### Technologies Used:
- SpigotAPI
- Gradle
- MongoDB
- MySQL
- SQLite
- InfluxDB

#### Plugin Hooks
- WorldEdit

------------

### Developer API
Check the wiki.

------------

### Some thoughts:
Cosmos is meant to be used as an API, with its design being intentionally modular so other plugins can register their own data containers (for storing worlds through any given database, such as Amazon S3) and serializers (for pasting worlds through any given format, such as SWM or .schem).
The idea behind Cosmos is that most plugins handle things like Arena Regeneration manually, and struggle to expand into more advanced structures that may involve saving a world to a database and loading it in another server. Cosmos is here to standardize that, and provide a simple interface to load any given template from any given source at any given time. 

Once the base of Cosmos is tested, a user management system will be implemented that allows admins to create templates in-game, through Cosmos directly, instead of relying on an external plugin.

If you'd like to help with the project, even as someone without any code experience, here are some things that would greatly help me out:

- Testing
- Helping with the wiki
- Writing issues
- Planning
- Donating (purely your choice, there's no benefit other than making me happy)
