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

package eu.cdevreeze.tqa.relationship

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import eu.cdevreeze.tqa.ENames.LinkCalculationArcEName
import eu.cdevreeze.tqa.dom.LabelArc
import eu.cdevreeze.tqa.dom.Linkbase
import eu.cdevreeze.tqa.dom.UriAwareTaxonomy
import eu.cdevreeze.tqa.dom.XsdSchema
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.indexed
import eu.cdevreeze.yaidom.parse.DocumentParserUsingStax

/**
 * Relationship extraction test case. It uses test data from the XBRL Core Conformance Suite.
 *
 * @author Chris de Vreeze
 */
@RunWith(classOf[JUnitRunner])
class ExtractRelationshipsTest extends FunSuite {

  test("testExtractRelationships") {
    // Using a simple linkbase and schema from the XBRL Core Conformance Suite.

    val docParser = DocumentParserUsingStax.newInstance()

    val xsdDocUri = classOf[ExtractRelationshipsTest].getResource("202-01-HrefResolution.xsd").toURI
    val linkbaseDocUri = classOf[ExtractRelationshipsTest].getResource("202-01-HrefResolution-label.xml").toURI

    val xsdDoc = indexed.Document(docParser.parse(xsdDocUri).withUriOption(Some(xsdDocUri)))
    val linkbaseDoc = indexed.Document(docParser.parse(linkbaseDocUri).withUriOption(Some(linkbaseDocUri)))

    val xsdSchema = XsdSchema.build(xsdDoc.documentElement)
    val linkbase = Linkbase.build(linkbaseDoc.documentElement)

    val tns = "http://mycompany.com/xbrl/taxonomy"

    val taxo = UriAwareTaxonomy.build(Vector(xsdSchema, linkbase))

    val relationshipsFactory = DefaultRelationshipsFactory.LenientInstance

    val relationships = relationshipsFactory.extractRelationships(taxo, RelationshipsFactory.AnyArc)

    val conceptLabelRelationships = relationships collect { case rel: ConceptLabelRelationship => rel }

    assertResult(2) {
      relationships.size
    }
    assertResult(relationships.map(_.arc)) {
      conceptLabelRelationships.map(_.arc)
    }

    assertResult(Set(
      (EName(tns, "fixedAssets"), "Fixed Assets"),
      (EName(tns, "changeInRetainedEarnings"), "Change in Retained Earnings"))) {

      conceptLabelRelationships.map(rel => (rel.sourceConceptEName, rel.labelText)).toSet
    }
  }

  test("testExtractRelationshipsUsingXmlBase") {
    // Using a simple linkbase and schema from the XBRL Core Conformance Suite, both with XML base attributes.

    val docParser = DocumentParserUsingStax.newInstance()

    val xsdDocUri = classOf[ExtractRelationshipsTest].getResource("202-03-HrefResolutionXMLBase.xsd").toURI
    val linkbaseDocUri = classOf[ExtractRelationshipsTest].getResource("base/202-03-HrefResolutionXMLBase-label.xml").toURI

    val xsdDoc = indexed.Document(docParser.parse(xsdDocUri).withUriOption(Some(xsdDocUri)))
    val linkbaseDoc = indexed.Document(docParser.parse(linkbaseDocUri).withUriOption(Some(linkbaseDocUri)))

    val xsdSchema = XsdSchema.build(xsdDoc.documentElement)
    val linkbase = Linkbase.build(linkbaseDoc.documentElement)

    val tns = "http://mycompany.com/xbrl/taxonomy"

    val taxo = UriAwareTaxonomy.build(Vector(xsdSchema, linkbase))

    val relationshipsFactory = DefaultRelationshipsFactory.LenientInstance

    val relationships = relationshipsFactory.extractRelationships(taxo, RelationshipsFactory.AnyArc)

    val conceptLabelRelationships = relationships collect { case rel: ConceptLabelRelationship => rel }

    assertResult(2) {
      relationships.size
    }
    assertResult(relationships.map(_.arc)) {
      conceptLabelRelationships.map(_.arc)
    }

    assertResult(Set(
      (EName(tns, "changeInRetainedEarnings"), "Fixed Assets"),
      (EName(tns, "fixedAssets"), "Change in Retained Earnings"))) {

      conceptLabelRelationships.map(rel => (rel.sourceConceptEName, rel.labelText)).toSet
    }
  }

  test("testExtractRelationshipsInEmbeddedLinkbase") {
    // Using an embedded linkbase from the XBRL Core Conformance Suite.

    val docParser = DocumentParserUsingStax.newInstance()

    val docUri = classOf[ExtractRelationshipsTest].getResource("292-00-Embeddedlinkbaseinthexsd.xsd").toURI

    val doc = indexed.Document(docParser.parse(docUri).withUriOption(Some(docUri)))

    val xsdSchema = XsdSchema.build(doc.documentElement)

    val tns = "http://www.UBmatrix.com/Patterns/BasicCalculation"

    val taxo = UriAwareTaxonomy.build(Vector(xsdSchema))

    val relationshipsFactory = DefaultRelationshipsFactory.LenientInstance

    val relationships = relationshipsFactory.extractRelationships(taxo, RelationshipsFactory.AnyArc)

    // Parent-child relationships

    val parentChildRelationshipParents: Set[EName] =
      (relationships collect { case rel: ParentChildRelationship => rel.sourceConceptEName }).toSet

    assertResult(Set(EName(tns, "PropertyPlantEquipment"))) {
      parentChildRelationshipParents
    }

    val parentChildRelationshipChildren: Set[EName] =
      (relationships collect { case rel: ParentChildRelationship => rel.targetConceptEName }).toSet

    assertResult(
      Set(
        EName(tns, "Building"),
        EName(tns, "ComputerEquipment"),
        EName(tns, "FurnitureFixtures"),
        EName(tns, "Land"),
        EName(tns, "Other"),
        EName(tns, "TotalPropertyPlantEquipment"))) {

        parentChildRelationshipChildren
      }

    assertResult(parentChildRelationshipChildren) {
      (relationships collect { case rel: ParentChildRelationship => rel.targetGlobalElementDeclaration.targetEName }).toSet
    }

    // Calculation relationships

    val calcRelationships = relationships collect { case rel: CalculationRelationship => rel }

    val total = EName(tns, "TotalPropertyPlantEquipment")

    assertResult(
      Set(
        (total, EName(tns, "Building")),
        (total, EName(tns, "ComputerEquipment")),
        (total, EName(tns, "FurnitureFixtures")),
        (total, EName(tns, "Land")),
        (total, EName(tns, "Other")))) {

        calcRelationships.map(rel => (rel.sourceConceptEName -> rel.targetConceptEName)).toSet
      }

    assertResult(calcRelationships.map(_.arc).toSet) {
      relationshipsFactory.extractRelationships(taxo, (_.resolvedName == LinkCalculationArcEName)).map(_.arc).toSet
    }

    // Concept-label relationships

    val computerEquipmentLabelRelationships =
      relationships collect { case rel: ConceptLabelRelationship if rel.sourceConceptEName == EName(tns, "ComputerEquipment") => rel }

    assertResult(2) {
      computerEquipmentLabelRelationships.size
    }
    assertResult(Set("http://www.xbrl.org/2003/role/label", "http://www.xbrl.org/2003/role/documentation")) {
      computerEquipmentLabelRelationships.map(_.resourceRole).toSet
    }
    assertResult(Set("en")) {
      computerEquipmentLabelRelationships.map(_.language).toSet
    }
    assertResult(Set("Computer Equipment", "Documentation for Computer Equipment")) {
      computerEquipmentLabelRelationships.map(_.labelText).toSet
    }

    assertResult(computerEquipmentLabelRelationships.map(_.arc).toSet) {
      relationshipsFactory.extractRelationships(
        taxo,
        (arc => arc.isInstanceOf[LabelArc] && arc.from == "ci_ComputerEquipment")).map(_.arc).toSet
    }
  }
}
