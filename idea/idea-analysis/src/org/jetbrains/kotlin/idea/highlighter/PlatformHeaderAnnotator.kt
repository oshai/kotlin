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

package org.jetbrains.kotlin.idea.highlighter

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.descriptors.MemberDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.DiagnosticSink
import org.jetbrains.kotlin.idea.caches.resolve.analyze
import org.jetbrains.kotlin.idea.caches.resolve.findModuleDescriptor
import org.jetbrains.kotlin.idea.project.TargetPlatformDetector
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.checkers.PlatformImplDeclarationChecker
import org.jetbrains.kotlin.resolve.diagnostics.Diagnostics
import org.jetbrains.kotlin.resolve.diagnostics.SimpleDiagnostics


class PlatformHeaderAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val declaration = element as? KtDeclaration ?: return

        val platform = TargetPlatformDetector.getPlatform(declaration.containingKtFile)
        if (!platform.kind.default) return

        val defaultModuleDescriptor = declaration.findModuleDescriptor()
        val dependentDescriptors = defaultModuleDescriptor.allImplementingModules

        val diagnostics = Validator(declaration.analyze(), dependentDescriptors).validate(declaration)
        KotlinPsiChecker().annotateElement(declaration, holder, diagnostics)
    }

    class Validator(
            val bindingContext: BindingContext,
            modulesToCheck: Collection<ModuleDescriptor>
    ) {

        private val diagnosticList = mutableListOf<Diagnostic>()

        private val diagnosticSink = object : DiagnosticSink {
            override fun report(diagnostic: Diagnostic) {
                diagnosticList += diagnostic
            }

            override fun wantsDiagnostics() = true
        }

        private val checkers = modulesToCheck.map(::PlatformImplDeclarationChecker)

        fun validate(declaration: KtDeclaration): Diagnostics {
            if (!declaration.hasModifier(KtTokens.PLATFORM_KEYWORD)) return Diagnostics.EMPTY
            val descriptor = bindingContext[BindingContext.DECLARATION_TO_DESCRIPTOR, declaration] as? MemberDescriptor
                             ?: return Diagnostics.EMPTY
            if (!descriptor.isPlatform) return Diagnostics.EMPTY
            for (checker in checkers) {
                checker.checkPlatformDeclarationHasDefinition(declaration, descriptor, diagnosticSink, checkImpl = false)
            }
            return if (diagnosticList.isNotEmpty()) SimpleDiagnostics(diagnosticList) else Diagnostics.EMPTY
        }
    }
}