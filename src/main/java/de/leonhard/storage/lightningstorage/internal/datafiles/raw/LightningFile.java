package de.leonhard.storage.lightningstorage.internal.datafiles.raw;

import de.leonhard.storage.lightningstorage.editor.LightningEditor;
import de.leonhard.storage.lightningstorage.internal.base.FileData;
import de.leonhard.storage.lightningstorage.internal.base.FlatFile;
import de.leonhard.storage.lightningstorage.internal.base.enums.ConfigSetting;
import de.leonhard.storage.lightningstorage.internal.base.enums.ReloadSetting;
import de.leonhard.storage.lightningstorage.utils.FileUtils;
import java.io.File;
import java.io.InputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@SuppressWarnings({"unused"})
public class LightningFile extends FlatFile {

	public LightningFile(@NotNull final File file, @Nullable final InputStream inputStream, @Nullable final ReloadSetting reloadSetting, @Nullable final ConfigSetting configSetting, @Nullable final FileData.Type fileDataType) {
		super(file, FileType.LIGHTNING);
		if (create() && inputStream != null) {
			FileUtils.writeToFile(this.file, inputStream);
		}

		if (configSetting != null) {
			setConfigSetting(configSetting);
		}
		if (fileDataType != null) {
			setFileDataType(fileDataType);
		}

		reload();
		if (reloadSetting != null) {
			setReloadSetting(reloadSetting);
		}
	}

	@Override
	public void reload() {
		this.fileData = new FileData(LightningEditor.readData(this.file, getFileDataType(), getConfigSetting()));
	}

	@Override
	public Object get(@NotNull final String key) {
		update();
		String finalKey = (this.getPathPrefix() == null) ? key : this.getPathPrefix() + "." + key;
		return fileData.get(finalKey);
	}

	@Override
	public synchronized void set(@NotNull final String key, @Nullable final Object value) {
		if (insert(key, value)) {
			try {
				LightningEditor.writeData(this.file, this.fileData.toMap(), getConfigSetting());
			} catch (IllegalStateException | IllegalArgumentException e) {
				System.err.println("Error while writing to '" + file.getAbsolutePath() + "'");
				e.printStackTrace();
				throw new IllegalStateException();
			}
		}
	}


	@Override
	public synchronized void remove(@NotNull final String key) {
		final String finalKey = (this.getPathPrefix() == null) ? key : this.getPathPrefix() + "." + key;

		update();

		if (fileData.containsKey(finalKey)) {
			fileData.remove(finalKey);

			try {
				LightningEditor.writeData(this.file, this.fileData.toMap(), getConfigSetting());
			} catch (IllegalStateException e) {
				System.err.println("Error while writing to '" + file.getAbsolutePath() + "'");
				e.printStackTrace();
				throw new IllegalStateException();
			}
		}
	}

	protected final LightningFile getLightningFileInstance() {
		return this;
	}

	@Override
	public boolean equals(@Nullable final Object obj) {
		if (obj == this) {
			return true;
		} else if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		} else {
			LightningFile lightningFile = (LightningFile) obj;
			return super.equals(lightningFile.getFlatFileInstance());
		}
	}
}