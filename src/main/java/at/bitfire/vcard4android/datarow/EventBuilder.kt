package at.bitfire.vcard4android.datarow

import android.net.Uri
import android.provider.ContactsContract.CommonDataKinds.Event
import at.bitfire.vcard4android.BatchOperation
import at.bitfire.vcard4android.Constants
import at.bitfire.vcard4android.Contact
import ezvcard.property.DateOrTimeProperty
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Level

class EventBuilder(mimeType: String, dataRowUri: Uri, rawContactId: Long?, contact: Contact)
    : DataRowBuilder(mimeType, dataRowUri, rawContactId, contact) {

    override fun build(): List<BatchOperation.CpoBuilder> {
        val result = LinkedList<BatchOperation.CpoBuilder>()

        buildEvent(contact.birthDay, Event.TYPE_BIRTHDAY)?.let { result += it }
        buildEvent(contact.anniversary, Event.TYPE_ANNIVERSARY)?.let { result += it }

        for (customDate in contact.customDates)
            buildEvent(customDate.property, Event.TYPE_CUSTOM, customDate.label)?.let { result += it }

        return result
    }

    fun buildEvent(dateOrTime: DateOrTimeProperty?, typeCode: Int, label: String? = null): BatchOperation.CpoBuilder? {
        if (dateOrTime == null)
            return null

        val dateStr: String
        dateStr = when {
            dateOrTime.date != null -> {
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
                format.format(dateOrTime.date)
            }
            dateOrTime.partialDate != null ->
                dateOrTime.partialDate.toString()
            else -> {
                Constants.log.log(Level.WARNING, "Ignoring date/time without (partial) date", dateOrTime)
                return null
            }
        }

        val builder = newDataRow()
            .withValue(Event.TYPE, typeCode)
            .withValue(Event.START_DATE, dateStr)

        if (label != null)
            builder.withValue(Event.LABEL, label)

        return builder
    }


    object Factory: DataRowBuilder.Factory<EventBuilder> {
        override fun mimeType() = Event.CONTENT_ITEM_TYPE
        override fun newInstance(dataRowUri: Uri, rawContactId: Long?, contact: Contact) =
            EventBuilder(mimeType(), dataRowUri, rawContactId, contact)
    }

}