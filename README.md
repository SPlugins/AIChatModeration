# AIChatModeration

An AI-powered chat moderation plugin for Minecraft servers (Spigot/Paper/Folia) that automatically detects and filters harmful messages using multiple AI providers.

## Features

- **Multi-Provider AI Support**: Integrates with OpenAI, Mistral, and Detoxify for comprehensive content moderation
- **Real-time Chat Filtering**: Automatically detects and blocks harmful messages
- **Customizable Categories**: Configure detection for various harmful content types (violence, harassment, hate speech, etc.)
- **Flexible Actions**: Define custom actions for different violation types (warnings, kicks, bans, custom commands)
- **Blacklist System**: Non-AI word filtering for instant blocking of specific terms
- **Permission-based Bypass**: Allow trusted players/staff to bypass moderation
- **Folia Support**: Full compatibility with Folia's regionized threading system
- **Extensive Logging**: Track flagged messages with customizable formats

## Requirements

- Java 8 or higher
- Minecraft Server: Spigot/Paper 1.13+ or Folia
- Maven for building from source

## Installation

### Pre-built JAR
1. Download the latest release from the [Releases](https://github.com/ssomar/AIChatModeration/releases) page
2. Place the JAR file in your server's `plugins` folder
3. Restart your server
4. Configure the plugin in `plugins/AIChatModeration/config.yml`

### Building from Source
```bash
git clone https://github.com/ssomar/AIChatModeration.git
cd AIChatModeration
mvn clean package
```
The compiled JAR will be in the `target` folder.

## Configuration

### Basic Setup

1. **Enable the plugin**:
```yaml
enabled: true
```

2. **Choose AI providers** (you can use multiple):
```yaml
providers: [DETOXIFY, OPENAI]
```

3. **Configure categories** you want to detect:
```yaml
categories:
  harassment:
    detection: true
    actions:
      warn:
        confidence: 0.80
        hideMessage: true
        commands:
          - "SEND_MESSAGE &6%player% &cYou are not allowed to harass other players."
```

### Provider Information

- **OpenAI**: Fast responses, good for English content
- **Mistral**: Better for Arabic and Russian languages
- **Detoxify**: Very fast, good for IT, FR, RU, PT, ES, TR languages

### Permission Nodes

| Permission | Description | Default |
|------------|-------------|---------|
| `acm.reload` | Allows using /acm reload command | op |
| `acm.debug` | Allows using /acm debug command | op |
| `acm.bypass` | Bypasses chat moderation | false |
| `acm.notify` | Receives notifications about flagged messages | false |

## Commands

- `/acm reload` - Reloads the configuration
- `/acm debug` - Toggles debug mode for troubleshooting

## Contributing

We welcome contributions! Here's how you can help:

### Development Setup

1. **Fork and Clone**:
```bash
git fork https://github.com/ssomar/AIChatModeration.git
git clone https://github.com/YOUR_USERNAME/AIChatModeration.git
cd AIChatModeration
```

2. **Set up your IDE**:
   - Import as Maven project
   - Install Lombok plugin for your IDE (required for annotations)
   - Configure Java 8 SDK

3. **Project Structure**:
```
src/main/java/com/ssomar/aichatmoderation/
├── AIChatModeration.java       # Main plugin class
├── actions/                    # Action handling system
├── categories/                 # Category configuration
├── commands/                   # Command handlers
├── listeners/                  # Event listeners
├── scheduler/                  # Scheduler abstraction (Bukkit/Folia)
└── utils/                      # Utility classes
```

### Adding New Features

#### Adding a New AI Provider

1. Create a new provider class in the appropriate package
2. Implement the provider interface with required methods:
   - Message analysis
   - Category detection
   - Confidence scoring
3. Add provider configuration to `config.yml`
4. Register the provider in `AIChatModeration.java`

#### Adding New Detection Categories

1. Update the `Category.java` class if needed
2. Add default configuration in `config.yml`
3. Document the new category in this README

#### Adding New Actions

1. Create a new action class extending `Action.java`
2. Implement the action logic
3. Register in the action system
4. Add configuration examples

### Code Style Guidelines

- Use meaningful variable and method names
- Follow Java naming conventions
- Add JavaDoc comments for public methods
- Keep methods focused and under 50 lines when possible
- Use Lombok annotations where appropriate
- Handle exceptions properly with logging

### Testing

Before submitting a PR:

1. **Test on multiple server versions**:
   - Paper 1.13+
   - Latest Paper version
   - Folia (if applicable)

2. **Test all features**:
   - Message detection with each provider
   - All action types
   - Permission checks
   - Command functionality
   - Configuration reloading

3. **Performance testing**:
   - Monitor TPS impact
   - Check async operation handling
   - Verify no memory leaks

### Submitting Pull Requests

1. Create a feature branch:
```bash
git checkout -b feature/your-feature-name
```

2. Make your changes and commit:
```bash
git add .
git commit -m "Add: Description of your changes"
```

3. Push to your fork:
```bash
git push origin feature/your-feature-name
```

4. Create a Pull Request with:
   - Clear description of changes
   - Testing done
   - Any breaking changes noted
   - Screenshots if UI-related

### Reporting Issues

When reporting bugs, please include:
- Server version and type (Paper/Spigot/Folia)
- Plugin version
- Full error logs
- Steps to reproduce
- Your config.yml (sensitive data removed)

## Support

- **Issues**: [GitHub Issues](https://github.com/ssomar/AIChatModeration/issues)
- **Discord**: [Join our Discord](#) (add your Discord link)
- **Wiki**: [Documentation Wiki](https://github.com/ssomar/AIChatModeration/wiki)

## License

This project is licensed under the [MIT License](LICENSE) - see the LICENSE file for details.

## Credits

- **Author**: Ssomar
- **Contributors**: See [Contributors](https://github.com/ssomar/AIChatModeration/contributors)
- **Libraries Used**:
  - Paper API
  - bStats (metrics)
  - Lombok
  - JSON Simple

## Changelog

See [CHANGELOG.md](CHANGELOG.md) for version history and updates.

---

Made with ❤️ by Ssomar for the Minecraft community