/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

package org.jetbrains.kotlin.resolve

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.incremental.components.LookupLocation
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.scopes.BaseImportingScope
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.utils.Printer

class SingleImportScope(private val aliasName: Name, private val descriptors: Collection<DeclarationDescriptor>) : BaseImportingScope(null) {
    override fun getContributedClassifier(name: Name, location: LookupLocation)
            = if (name == aliasName) descriptors.filterIsInstance<ClassifierDescriptor>().singleOrNull() else null

    override fun getContributedPackage(name: Name)
            = if (name == aliasName) descriptors.filterIsInstance<PackageViewDescriptor>().singleOrNull() else null

    override fun getContributedVariables(name: Name, location: LookupLocation)
            = if (name == aliasName) descriptors.filterIsInstance<VariableDescriptor>() else emptyList()

    override fun getContributedFunctions(name: Name, location: LookupLocation)
            = if (name == aliasName) descriptors.filterIsInstance<FunctionDescriptor>() else emptyList()

    override fun getContributedDescriptors(kindFilter: DescriptorKindFilter, nameFilter: (Name) -> Boolean)
            = descriptors

    override fun printStructure(p: Printer) {
        p.println(javaClass.simpleName, ": ", aliasName)
    }
}
