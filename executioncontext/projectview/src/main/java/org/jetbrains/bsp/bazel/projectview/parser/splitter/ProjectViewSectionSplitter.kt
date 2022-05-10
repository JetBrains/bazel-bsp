package org.jetbrains.bsp.bazel.projectview.parser.splitter

import java.util.regex.Pattern
import java.util.stream.Collectors

/**
 * Splitter is responsible for splitting file content into "raw" sections - section name and entire
 * section body. Also, it removes comments (starting with '#') from section content
 *
 *
 * e.g.: <br></br>
 * file content:
 *
 * <pre>`
 * ---
 * import path/to/another/file.bazelproject
 *
 * section1: value1 # comment
 *
 * section2:
 * included_value1
 * included_value2
 * -excluded_value3
 * ---
`</pre> *
 *
 *
 * will be split into raw sections:<br></br>
 * 1) `'import'` -- `'path/to/another/file.bazelproject\n\n'`<br></br>
 * 2) `'section1'` -- `'value1 \n\n'`<br></br>
 * 3) `'section2'` -- `
 * '\n  included_value1\n  included_value2\n  -excluded_value3\n'`<br></br>
 */
object ProjectViewSectionSplitter {

    private val SECTION_HEADER_REGEX = Pattern.compile("((^[^:\\-/*\\s]+)([: ]))", Pattern.MULTILINE)
    private const val SECTION_HEADER_NAME_GROUP_ID = 2

    private val COMMENT_LINE_REGEX = "#(.)*(\\n|\\z)".toRegex()
    private const val COMMENT_LINE_REPLACEMENT = "\n"

    fun getRawSectionsForFileContent(fileContent: String): ProjectViewRawSections {
        val fileContentWithoutComments = removeLinesWithComments(fileContent)
        val rawSections = findRawSections(fileContentWithoutComments)

        return ProjectViewRawSections(rawSections)
    }

    private fun removeLinesWithComments(fileContent: String): String =
        fileContent.replace(COMMENT_LINE_REGEX, COMMENT_LINE_REPLACEMENT)

    private fun findRawSections(fileContent: String): List<ProjectViewRawSection> {
        val sectionHeadersNames = findSectionsHeadersNames(fileContent)
        val sectionBodies = findSectionsBodiesAndSkipFirstEmptyEntry(fileContent)

        return sectionHeadersNames
            .zip(sectionBodies) { sectionName, sectionBody ->
                ProjectViewRawSection(sectionName, sectionBody)
            }
    }

    private fun findSectionsHeadersNames(fileContent: String): List<String> =
        SECTION_HEADER_REGEX.matcher(fileContent)
            .results()
            .map { it.group(SECTION_HEADER_NAME_GROUP_ID) }
            .collect(Collectors.toList())

    private fun findSectionsBodiesAndSkipFirstEmptyEntry(fileContent: String): List<String> =
        SECTION_HEADER_REGEX.split(fileContent).toList().drop(1)
}
