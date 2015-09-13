package com.nd.android.sdp.dm.provider.downloads;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.nd.android.sdp.dm.provider.base.AbstractSelection;

import java.util.Date;

/**
 * Selection for the {@code downloads} table.
 */
public class DownloadsSelection extends AbstractSelection<DownloadsSelection> {
    @Override
    protected Uri baseUri() {
        return DownloadsColumns.CONTENT_URI;
    }

    /**
     * Query the given content resolver using this selection.
     *
     * @param contentResolver The content resolver to query.
     * @param projection A list of which columns to return. Passing null will return all columns, which is inefficient.
     * @return A {@code DownloadsCursor} object, which is positioned before the first entry, or null.
     */
    public DownloadsCursor query(ContentResolver contentResolver, String[] projection) {
        Cursor cursor = contentResolver.query(uri(), projection, sel(), args(), order());
        if (cursor == null) return null;
        return new DownloadsCursor(cursor);
    }

    /**
     * Equivalent of calling {@code query(contentResolver, null)}.
     */
    public DownloadsCursor query(ContentResolver contentResolver) {
        return query(contentResolver, null);
    }

    /**
     * Query the given content resolver using this selection.
     *
     * @param context The context to use for the query.
     * @param projection A list of which columns to return. Passing null will return all columns, which is inefficient.
     * @return A {@code DownloadsCursor} object, which is positioned before the first entry, or null.
     */
    public DownloadsCursor query(Context context, String[] projection) {
        Cursor cursor = context.getContentResolver().query(uri(), projection, sel(), args(), order());
        if (cursor == null) return null;
        return new DownloadsCursor(cursor);
    }

    /**
     * Equivalent of calling {@code query(context, null)}.
     */
    public DownloadsCursor query(Context context) {
        return query(context, null);
    }


    public DownloadsSelection id(long... value) {
        addEquals("downloads." + DownloadsColumns._ID, toObjectArray(value));
        return this;
    }

    public DownloadsSelection idNot(long... value) {
        addNotEquals("downloads." + DownloadsColumns._ID, toObjectArray(value));
        return this;
    }

    public DownloadsSelection orderById(boolean desc) {
        orderBy("downloads." + DownloadsColumns._ID, desc);
        return this;
    }

    public DownloadsSelection orderById() {
        return orderById(false);
    }

    public DownloadsSelection url(String... value) {
        addEquals(DownloadsColumns.URL, value);
        return this;
    }

    public DownloadsSelection urlNot(String... value) {
        addNotEquals(DownloadsColumns.URL, value);
        return this;
    }

    public DownloadsSelection urlLike(String... value) {
        addLike(DownloadsColumns.URL, value);
        return this;
    }

    public DownloadsSelection urlContains(String... value) {
        addContains(DownloadsColumns.URL, value);
        return this;
    }

    public DownloadsSelection urlStartsWith(String... value) {
        addStartsWith(DownloadsColumns.URL, value);
        return this;
    }

    public DownloadsSelection urlEndsWith(String... value) {
        addEndsWith(DownloadsColumns.URL, value);
        return this;
    }

    public DownloadsSelection orderByUrl(boolean desc) {
        orderBy(DownloadsColumns.URL, desc);
        return this;
    }

    public DownloadsSelection orderByUrl() {
        orderBy(DownloadsColumns.URL, false);
        return this;
    }

    public DownloadsSelection filepath(String... value) {
        addEquals(DownloadsColumns.FILEPATH, value);
        return this;
    }

    public DownloadsSelection filepathNot(String... value) {
        addNotEquals(DownloadsColumns.FILEPATH, value);
        return this;
    }

    public DownloadsSelection filepathLike(String... value) {
        addLike(DownloadsColumns.FILEPATH, value);
        return this;
    }

    public DownloadsSelection filepathContains(String... value) {
        addContains(DownloadsColumns.FILEPATH, value);
        return this;
    }

    public DownloadsSelection filepathStartsWith(String... value) {
        addStartsWith(DownloadsColumns.FILEPATH, value);
        return this;
    }

    public DownloadsSelection filepathEndsWith(String... value) {
        addEndsWith(DownloadsColumns.FILEPATH, value);
        return this;
    }

    public DownloadsSelection orderByFilepath(boolean desc) {
        orderBy(DownloadsColumns.FILEPATH, desc);
        return this;
    }

    public DownloadsSelection orderByFilepath() {
        orderBy(DownloadsColumns.FILEPATH, false);
        return this;
    }

    public DownloadsSelection md5(String... value) {
        addEquals(DownloadsColumns.MD5, value);
        return this;
    }

    public DownloadsSelection md5Not(String... value) {
        addNotEquals(DownloadsColumns.MD5, value);
        return this;
    }

    public DownloadsSelection md5Like(String... value) {
        addLike(DownloadsColumns.MD5, value);
        return this;
    }

    public DownloadsSelection md5Contains(String... value) {
        addContains(DownloadsColumns.MD5, value);
        return this;
    }

    public DownloadsSelection md5StartsWith(String... value) {
        addStartsWith(DownloadsColumns.MD5, value);
        return this;
    }

    public DownloadsSelection md5EndsWith(String... value) {
        addEndsWith(DownloadsColumns.MD5, value);
        return this;
    }

    public DownloadsSelection orderByMd5(boolean desc) {
        orderBy(DownloadsColumns.MD5, desc);
        return this;
    }

    public DownloadsSelection orderByMd5() {
        orderBy(DownloadsColumns.MD5, false);
        return this;
    }

    public DownloadsSelection state(Integer... value) {
        addEquals(DownloadsColumns.STATE, value);
        return this;
    }

    public DownloadsSelection stateNot(Integer... value) {
        addNotEquals(DownloadsColumns.STATE, value);
        return this;
    }

    public DownloadsSelection stateGt(int value) {
        addGreaterThan(DownloadsColumns.STATE, value);
        return this;
    }

    public DownloadsSelection stateGtEq(int value) {
        addGreaterThanOrEquals(DownloadsColumns.STATE, value);
        return this;
    }

    public DownloadsSelection stateLt(int value) {
        addLessThan(DownloadsColumns.STATE, value);
        return this;
    }

    public DownloadsSelection stateLtEq(int value) {
        addLessThanOrEquals(DownloadsColumns.STATE, value);
        return this;
    }

    public DownloadsSelection orderByState(boolean desc) {
        orderBy(DownloadsColumns.STATE, desc);
        return this;
    }

    public DownloadsSelection orderByState() {
        orderBy(DownloadsColumns.STATE, false);
        return this;
    }

    public DownloadsSelection moduleName(String... value) {
        addEquals(DownloadsColumns.MODULE_NAME, value);
        return this;
    }

    public DownloadsSelection moduleNameNot(String... value) {
        addNotEquals(DownloadsColumns.MODULE_NAME, value);
        return this;
    }

    public DownloadsSelection moduleNameLike(String... value) {
        addLike(DownloadsColumns.MODULE_NAME, value);
        return this;
    }

    public DownloadsSelection moduleNameContains(String... value) {
        addContains(DownloadsColumns.MODULE_NAME, value);
        return this;
    }

    public DownloadsSelection moduleNameStartsWith(String... value) {
        addStartsWith(DownloadsColumns.MODULE_NAME, value);
        return this;
    }

    public DownloadsSelection moduleNameEndsWith(String... value) {
        addEndsWith(DownloadsColumns.MODULE_NAME, value);
        return this;
    }

    public DownloadsSelection orderByModuleName(boolean desc) {
        orderBy(DownloadsColumns.MODULE_NAME, desc);
        return this;
    }

    public DownloadsSelection orderByModuleName() {
        orderBy(DownloadsColumns.MODULE_NAME, false);
        return this;
    }

    public DownloadsSelection currentSize(Long... value) {
        addEquals(DownloadsColumns.CURRENT_SIZE, value);
        return this;
    }

    public DownloadsSelection currentSizeNot(Long... value) {
        addNotEquals(DownloadsColumns.CURRENT_SIZE, value);
        return this;
    }

    public DownloadsSelection currentSizeGt(long value) {
        addGreaterThan(DownloadsColumns.CURRENT_SIZE, value);
        return this;
    }

    public DownloadsSelection currentSizeGtEq(long value) {
        addGreaterThanOrEquals(DownloadsColumns.CURRENT_SIZE, value);
        return this;
    }

    public DownloadsSelection currentSizeLt(long value) {
        addLessThan(DownloadsColumns.CURRENT_SIZE, value);
        return this;
    }

    public DownloadsSelection currentSizeLtEq(long value) {
        addLessThanOrEquals(DownloadsColumns.CURRENT_SIZE, value);
        return this;
    }

    public DownloadsSelection orderByCurrentSize(boolean desc) {
        orderBy(DownloadsColumns.CURRENT_SIZE, desc);
        return this;
    }

    public DownloadsSelection orderByCurrentSize() {
        orderBy(DownloadsColumns.CURRENT_SIZE, false);
        return this;
    }

    public DownloadsSelection totalSize(Long... value) {
        addEquals(DownloadsColumns.TOTAL_SIZE, value);
        return this;
    }

    public DownloadsSelection totalSizeNot(Long... value) {
        addNotEquals(DownloadsColumns.TOTAL_SIZE, value);
        return this;
    }

    public DownloadsSelection totalSizeGt(long value) {
        addGreaterThan(DownloadsColumns.TOTAL_SIZE, value);
        return this;
    }

    public DownloadsSelection totalSizeGtEq(long value) {
        addGreaterThanOrEquals(DownloadsColumns.TOTAL_SIZE, value);
        return this;
    }

    public DownloadsSelection totalSizeLt(long value) {
        addLessThan(DownloadsColumns.TOTAL_SIZE, value);
        return this;
    }

    public DownloadsSelection totalSizeLtEq(long value) {
        addLessThanOrEquals(DownloadsColumns.TOTAL_SIZE, value);
        return this;
    }

    public DownloadsSelection orderByTotalSize(boolean desc) {
        orderBy(DownloadsColumns.TOTAL_SIZE, desc);
        return this;
    }

    public DownloadsSelection orderByTotalSize() {
        orderBy(DownloadsColumns.TOTAL_SIZE, false);
        return this;
    }

    public DownloadsSelection createTime(Date... value) {
        addEquals(DownloadsColumns.CREATE_TIME, value);
        return this;
    }

    public DownloadsSelection createTimeNot(Date... value) {
        addNotEquals(DownloadsColumns.CREATE_TIME, value);
        return this;
    }

    public DownloadsSelection createTime(Long... value) {
        addEquals(DownloadsColumns.CREATE_TIME, value);
        return this;
    }

    public DownloadsSelection createTimeAfter(Date value) {
        addGreaterThan(DownloadsColumns.CREATE_TIME, value);
        return this;
    }

    public DownloadsSelection createTimeAfterEq(Date value) {
        addGreaterThanOrEquals(DownloadsColumns.CREATE_TIME, value);
        return this;
    }

    public DownloadsSelection createTimeBefore(Date value) {
        addLessThan(DownloadsColumns.CREATE_TIME, value);
        return this;
    }

    public DownloadsSelection createTimeBeforeEq(Date value) {
        addLessThanOrEquals(DownloadsColumns.CREATE_TIME, value);
        return this;
    }

    public DownloadsSelection orderByCreateTime(boolean desc) {
        orderBy(DownloadsColumns.CREATE_TIME, desc);
        return this;
    }

    public DownloadsSelection orderByCreateTime() {
        orderBy(DownloadsColumns.CREATE_TIME, false);
        return this;
    }
}
