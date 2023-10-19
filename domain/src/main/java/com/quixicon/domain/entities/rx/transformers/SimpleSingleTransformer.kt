package com.quixicon.domain.entities.rx.transformers

import io.reactivex.SingleTransformer

/**
 * Interface for simplify access to the [SingleTransformer] interface.
 */
interface SimpleSingleTransformer<T> : SingleTransformer<T, T>
