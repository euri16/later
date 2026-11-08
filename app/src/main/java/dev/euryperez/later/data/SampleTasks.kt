package dev.euryperez.later.data

import java.time.LocalDate
import kotlin.random.Random

object SampleTasks {
    private val titles = mapOf(
        Category.WORK to listOf(
            "Update the onboarding deck", "Reply to the recruiter", "Write the quarterly report",
            "Clean up the Jira board", "Review the design specs", "Prepare the demo",
            "Fix the flaky build", "Answer the long email", "Book the team retro",
        ),
        Category.HOME to listOf(
            "Fold the laundry", "Fix the leaking faucet", "Clean the fridge",
            "Organize the closet", "Water the plants", "Change the light bulb",
            "Vacuum under the couch", "Take out the recycling", "Defrost the freezer",
        ),
        Category.HEALTH to listOf(
            "Call the dentist", "Go for a run", "Book the eye exam",
            "Drink more water", "Stretch for ten minutes", "Meal prep for the week",
            "Schedule the checkup", "Sleep before midnight", "Refill the prescription",
        ),
        Category.ADMIN to listOf(
            "Renew the passport", "File the taxes", "Cancel the unused subscription",
            "Update the insurance", "Pay the parking ticket", "Back up the phone",
            "Sort the receipts", "Change the bank password", "Read the lease renewal",
        ),
        Category.FUN to listOf(
            "Finish that book", "Learn three guitar chords", "Plan the beach weekend",
            "Watch the documentary", "Call an old friend", "Try the new pupusería",
            "Start the side project", "Visit the museum", "Build the Lego set",
        ),
    )

    private val notes = listOf(
        "",
        "It will only take five minutes. Probably.",
        "Do it before it becomes urgent.",
        "Future me will handle this.",
        "Seriously, this time.",
        "Needs coffee first.",
    )

    fun history(taskId: Long, task: Task): List<Postponement> {
        val now = System.currentTimeMillis()
        return List(task.postponeCount) { step ->
            val to = task.dueDate.minusDays(step.toLong())
            Postponement(
                taskId = taskId,
                fromDate = to.minusDays(1),
                toDate = to,
                timestamp = now - (step + 1) * 86_400_000L,
            )
        }
    }

    fun generate(count: Int, random: Random = Random(seed = 42)): List<Task> {
        val today = LocalDate.now()
        return List(count) { index ->
            val category = Category.entries[random.nextInt(Category.entries.size)]
            val base = titles.getValue(category)
            Task(
                title = "${base[random.nextInt(base.size)]} #${index + 1}",
                notes = notes[random.nextInt(notes.size)],
                category = category,
                dueDate = today.plusDays(random.nextLong(-20, 30)),
                postponeCount = random.nextInt(0, 25),
                isDone = random.nextInt(10) == 0,
            )
        }
    }
}
