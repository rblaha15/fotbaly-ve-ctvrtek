package cz.rblaha15.fotbaly_ve_ctvrtek

import io.kvision.core.BsColor
import io.kvision.core.Container
import io.kvision.core.addBsColor
import io.kvision.core.onClick
import io.kvision.core.onInput
import io.kvision.form.text.text
import io.kvision.html.ButtonStyle
import io.kvision.html.Div
import io.kvision.html.button
import io.kvision.html.div
import io.kvision.html.icon
import io.kvision.html.main
import io.kvision.html.p
import io.kvision.navbar.NavbarExpand
import io.kvision.navbar.NavbarType
import io.kvision.navbar.navbar
import io.kvision.state.bind
import io.kvision.utils.rem
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLIFrameElement

fun Container.mainContent(
    viewModel: ManViewModel,
) {
    navBar()
    main {
        addCssClass("container")
        addCssClass("my-3")

        nameInput(viewModel)
        counter(viewModel)
        myAnswerSelector(viewModel)
        answerList(viewModel)
    }
}

fun Container.counter(viewModel: ManViewModel) {
    p().bind(viewModel.count) {
        content = "Kolik nás bude: $it"
    }
}

private fun Container.navBar() = navbar(
    label = "Fotbaly ve čtvrtek",
    type = NavbarType.STICKYTOP,
    expand = NavbarExpand.ALWAYS,
) {
    val userAgent = window.navigator.userAgent.lowercase()
    if ("android" in userAgent) {
        button(
            "App",
            style = ButtonStyle.OUTLINESECONDARY,
        ) {
            onClick {
                val url = "intent://fotbaly-ve-ctvrtek.web.app#Intent;scheme=https;package=cz.rblaha15.fotbaly_ve_ctvrtek;end"
                (document.createElement("iframe") as HTMLIFrameElement).apply {
                    src = url
                    onload = {
                        window.open("https://github.com/rblaha15/fotbaly-ve-ctvrtek/release/download/latest/fotbaly-ve-ctvrtek.apk")
                    }
                    style.display = "none"
                    document.body?.appendChild(this)

                    window.setTimeout({
                        document.body?.removeChild(this)
                    }, 1000)
                }
            }
        }
    }
}

private fun Container.answerList(viewModel: ManViewModel) = div()
    .bind(viewModel.answers) { answers ->
        addCssClass("mt-3")
        answers.forEach { (name, answer) ->
            div {
                addCssClass("d-flex")
                addCssClass("flex-row")
                addCssClass("align-items-center")
                p {
                    addCssClass("flex-grow-1")
                    +name
                }
                answers(answer)
            }
        }
    }

private fun Container.myAnswerSelector(viewModel: ManViewModel) = div()
    .bind(viewModel.myAnswer) { myAnswer ->
        addCssClass("d-flex")
        addCssClass("flex-row")
        addCssClass("align-items-center")
        addCssClass("mt-3")
        p {
            addCssClass("flex-grow-1")
            +"Půjdeš ve čtvrtek na fotbal?"
        }
        answers(
            selected = myAnswer,
            onClick = { state ->
                viewModel.setMyAnswer(state)
            }
        )
    }

private fun Container.nameInput(viewModel: ManViewModel) = text(
    label = "Jméno",
    floating = true,
) {
    onInput {
        viewModel.setName(value ?: "")
    }
}.bind(viewModel.name) {
    value = it
}

private fun Div.answers(
    selected: AnswerState?,
    onClick: ((AnswerState) -> Unit)? = null,
) = AnswerState.entries.forEach { state ->
    icon(
        when (state) {
            AnswerState.Yes -> "bi-check"
            AnswerState.No -> "bi-x"
            AnswerState.Maybe -> "bi-question"
        }
    ) {
        addCssClass("ms-2")
        addCssClass("btn")
        if (onClick == null) addCssClass("disabled")
        setStyle("border", "none")
        setStyle("opacity", "1")
        onClick?.let { this.onClick { onClick(state) } }
        fontSize = 1.5.rem
        addBsColor(
            when {
                state == selected -> when (state) {
                    AnswerState.Yes -> BsColor.SUCCESS
                    AnswerState.No -> BsColor.DANGER
                    AnswerState.Maybe -> BsColor.INFO
                }

                else -> BsColor.BODY
            }
        )
    }
}