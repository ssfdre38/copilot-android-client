package com.github.copilot.client.model

data class QuickAction(
    val id: String,
    val title: String,
    val template: String,
    val icon: String? = null
)

object QuickActionTemplates {
    val COMMON_ACTIONS = listOf(
        QuickAction(
            id = "explain",
            title = "Explain Code",
            template = "Please explain this code:",
            icon = "💡"
        ),
        QuickAction(
            id = "shell",
            title = "Shell Command",
            template = "I need a shell command to:",
            icon = "⚡"
        ),
        QuickAction(
            id = "git",
            title = "Git Help",
            template = "Help me with git to:",
            icon = "🔧"
        ),
        QuickAction(
            id = "debug",
            title = "Debug",
            template = "Debug this error:",
            icon = "🐛"
        ),
        QuickAction(
            id = "optimize",
            title = "Optimize",
            template = "How can I optimize this code:",
            icon = "⚡"
        ),
        QuickAction(
            id = "test",
            title = "Write Tests",
            template = "Write unit tests for:",
            icon = "🧪"
        ),
        QuickAction(
            id = "document",
            title = "Documentation",
            template = "Add documentation for:",
            icon = "📝"
        ),
        QuickAction(
            id = "refactor",
            title = "Refactor",
            template = "How should I refactor:",
            icon = "🔄"
        ),
        QuickAction(
            id = "security",
            title = "Security Review",
            template = "Review security of:",
            icon = "🔒"
        ),
        QuickAction(
            id = "performance",
            title = "Performance",
            template = "Improve performance of:",
            icon = "⚡"
        )
    )
}