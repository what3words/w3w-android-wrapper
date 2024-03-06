package com.what3words.androidwrapper.datasource.text.api.mappers

import com.what3words.androidwrapper.common.Mapper
import com.what3words.androidwrapper.datasource.text.api.dto.LineDto
import com.what3words.androidwrapper.datasource.text.api.response.GridSectionResponse
import com.what3words.core.types.geometry.W3WGridSection
import com.what3words.core.types.geometry.W3WLine

internal class GridSectionResponseMapper(private val lineDtoToDomainMapper: Mapper<LineDto, W3WLine>) :
    Mapper<GridSectionResponse, W3WGridSection> {
    override fun mapFrom(from: GridSectionResponse): W3WGridSection {
        val lines = from.lines?.map { line: LineDto ->
            lineDtoToDomainMapper.mapFrom(line)
        } ?: emptyList()
        return W3WGridSection(
            lines = lines
        )
    }
}