/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.idea.quickfix.migration

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.idea.quickfix.CleanupFix
import org.jetbrains.kotlin.idea.quickfix.KotlinQuickFixAction
import org.jetbrains.kotlin.idea.quickfix.KotlinSingleIntentionActionFactory
import org.jetbrains.kotlin.idea.util.addAnnotation
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.*

class MigrateExternalFix(declaration: KtDeclaration)
    : KotlinQuickFixAction<KtDeclaration>(declaration), CleanupFix {

    override fun getText() = "Replace with 'asDynamic'"
    override fun getFamilyName() = getText()

    override fun invoke(project: Project, editor: Editor?, file: KtFile) {
        val declaration = element ?: return
        val name = declaration.name ?: return
        declaration.modifierList?.annotationEntries?.firstOrNull { it.typeReference?.text == "native" }?.delete()
        declaration.addModifier(KtTokens.INLINE_KEYWORD)
        declaration.removeModifier(KtTokens.EXTERNAL_KEYWORD)
        declaration.addAnnotation(KotlinBuiltIns.FQ_NAMES.suppress.toSafe(), "\"NOTHING_TO_INLINE\"")

        val ktPsiFactory = KtPsiFactory(project)
        val body = ktPsiFactory.buildExpression {
            appendName(Name.identifier("asDynamic"))
            appendFixedText("().")
            appendName(Name.identifier(name))
            appendFixedText("(")
            if (declaration is KtNamedFunction) {
                for ((index, param) in declaration.valueParameters.withIndex()) {
                    param.nameAsName?.let { paramName ->
                        if (index > 0) {
                            appendFixedText(",")
                        }
                        appendName(paramName)
                    }
                }
            }
            appendFixedText(")")
        }

        if (declaration is KtNamedFunction) {
            declaration.bodyExpression?.replace(body) ?: run {
                declaration.add(ktPsiFactory.createEQ())
                declaration.add(body)
            }
        }
    }

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile): Boolean {
        return super.isAvailable(project, editor, file)
    }

    companion object : KotlinSingleIntentionActionFactory() {
        override fun createAction(diagnostic: Diagnostic): IntentionAction? {
            val e = diagnostic.psiElement
            if (e is KtDeclaration) {
                return MigrateExternalFix(e)
            }
            return null
        }
    }
}
