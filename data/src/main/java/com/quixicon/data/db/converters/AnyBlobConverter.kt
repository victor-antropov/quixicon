package com.quixicon.data.db.converters

import androidx.room.TypeConverter
import com.quixicon.domain.entities.db.CardExtraData
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

object AnyBlobConverter {

    @TypeConverter
    @JvmStatic
    fun cardExtraDataToBlob(extraData: CardExtraData?) =
        extraData?.let {
            ByteArrayOutputStream().use { bos ->
                ObjectOutputStream(bos).use { oos ->
                    oos.writeObject(it)
                    bos.toByteArray()
                }
            }
        }

    @TypeConverter
    @JvmStatic
    fun blobToCardExtraData(blob: ByteArray?) =
        blob?.let {
            ByteArrayInputStream(blob).use { bos ->
                ObjectInputStream(bos).use { ois ->
                    @Suppress("UNCHECKED_CAST")
                    ois.readObject() as CardExtraData
                }
            }
        }
}
