package com.quixicon.presentation.fragments.test.models

import com.quixicon.domain.entities.enums.TestDirection

class TestDirectionModel(
    val name: String,
    val value: TestDirection
) {

    override fun toString(): String {
        return name
    }
}
