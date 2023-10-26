/*
 * Copyright 2020-2023 IceMimosa, jxnu-liguobin and the Bitlap Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bitlap.common.test

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.bitlap.common.BitlapIterator

/**
 * Mail: chk19940609@gmail.com
 * Created by IceMimosa
 * Date: 2021/7/16
 */
class BitlapIteratorTest : StringSpec({

    "Test simple BitlapIterator" {
        val iterator = BitlapIterator.of((1..100))
        iterator.asSequence().sum() shouldBe 5050
        val batchIterator = BitlapIterator.batch((1..100), 10)
        batchIterator.asSequence()
            .map { it * 2 }
            .sum() shouldBe 5050 * 2
    }
})
