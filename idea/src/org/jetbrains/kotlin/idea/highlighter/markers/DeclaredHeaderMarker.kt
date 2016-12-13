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

package org.jetbrains.kotlin.idea.highlighter.markers

import com.intellij.codeInsight.daemon.impl.PsiElementListNavigator
import com.intellij.ide.util.DefaultPsiElementCellRenderer
import com.intellij.psi.NavigatablePsiElement
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.idea.caches.resolve.findModuleDescriptor
import org.jetbrains.kotlin.idea.core.toDescriptor
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.resolve.DescriptorToSourceUtils
import org.jetbrains.kotlin.resolve.checkers.HeaderImplDeclarationChecker
import java.awt.event.MouseEvent

fun ModuleDescriptor.headerModuleOrNull() = allDependencyModules.filter {
    it.platformKind == PlatformKind.DEFAULT && it.sourceKind == sourceKind
}.firstOrNull()

fun ModuleDescriptor.hasDeclarationOf(descriptor: MemberDescriptor) = declarationOf(descriptor) != null

private fun ModuleDescriptor.declarationOf(descriptor: MemberDescriptor) =
        with (HeaderImplDeclarationChecker(this)) {
            when (descriptor) {
                is CallableMemberDescriptor -> {
                    descriptor.findNamesakesFromTheSameModule().filter { impl ->
                        impl.isHeader &&
                        DescriptorToSourceUtils.getSourceFromDescriptor(impl) is KtElement
                    }.firstOrNull()
                }
                is ClassDescriptor -> {
                    descriptor.findClassifiersFromTheSameModule().filter { impl ->
                        impl is ClassDescriptor && impl.isHeader &&
                        DescriptorToSourceUtils.getSourceFromDescriptor(impl) is KtElement
                    }.firstOrNull()
                }
                else -> null
            }
        }

fun getHeaderDeclarationTooltip(declaration: KtDeclaration): String? {
    val descriptor = declaration.toDescriptor() as? MemberDescriptor ?: return null
    val implementationModuleDescriptor = declaration.containingKtFile.findModuleDescriptor()

    val headerModuleDescriptor = implementationModuleDescriptor.headerModuleOrNull() ?: return null
    if (!headerModuleDescriptor.hasDeclarationOf(descriptor)) return null

    return "Has declaration in common module"
}

fun navigateToHeaderDeclaration(e: MouseEvent?, declaration: KtDeclaration) {
    val descriptor = declaration.toDescriptor() as? MemberDescriptor ?: return
    val implementationModuleDescriptor = declaration.containingKtFile.findModuleDescriptor()

    val headerModuleDescriptor = implementationModuleDescriptor.headerModuleOrNull() ?: return
    val headerDeclaration = DescriptorToSourceUtils.descriptorToDeclaration(
            headerModuleDescriptor.declarationOf(descriptor) ?: return
    ) as? NavigatablePsiElement ?: return

    val renderer = DefaultPsiElementCellRenderer()
    PsiElementListNavigator.openTargets(e,
                                        arrayOf(headerDeclaration),
                                        "",
                                        "Declaration of ${declaration.name}",
                                        renderer)
}
