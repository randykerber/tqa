/*
 * Copyright 2011-2017 Chris de Vreeze
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

package eu.cdevreeze.tqa.base.queryapi

import scala.collection.immutable
import scala.reflect.classTag

import eu.cdevreeze.tqa.XmlFragmentKey
import eu.cdevreeze.tqa.base.relationship.ElementLabelRelationship

/**
 * Partial implementation of `ElementLabelRelationshipContainerApi`.
 *
 * @author Chris de Vreeze
 */
trait ElementLabelRelationshipContainerLike extends ElementLabelRelationshipContainerApi { self: NonStandardRelationshipContainerApi =>

  // Finding and filtering relationships without looking at source element

  final def findAllElementLabelRelationships: immutable.IndexedSeq[ElementLabelRelationship] = {
    findAllNonStandardRelationshipsOfType(classTag[ElementLabelRelationship])
  }

  final def filterElementLabelRelationships(
    p: ElementLabelRelationship => Boolean): immutable.IndexedSeq[ElementLabelRelationship] = {

    filterNonStandardRelationshipsOfType(classTag[ElementLabelRelationship])(p)
  }

  // Finding and filtering outgoing relationships

  final def findAllOutgoingElementLabelRelationships(
    sourceKey: XmlFragmentKey): immutable.IndexedSeq[ElementLabelRelationship] = {

    findAllOutgoingNonStandardRelationshipsOfType(sourceKey, classTag[ElementLabelRelationship])
  }

  final def filterOutgoingElementLabelRelationships(
    sourceKey: XmlFragmentKey)(p: ElementLabelRelationship => Boolean): immutable.IndexedSeq[ElementLabelRelationship] = {

    filterOutgoingNonStandardRelationshipsOfType(sourceKey, classTag[ElementLabelRelationship])(p)
  }
}
