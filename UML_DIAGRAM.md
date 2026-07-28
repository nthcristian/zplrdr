# ZPL-RDR – UML Class Diagram

```mermaid
classDiagram
    direction TB

    %% Interfaces (contract package)
    class ConversionDriver {
        <<interface>>
        +requestConversion(ZPLDocument[], Preset) PDFDocument[]*
    }

    class PresetFieldManager {
        <<interface>>
        +validateFor(Map~String, String~ fields)*
        +getDefaultValueFor(String field) Object
        +getFieldNames() Set~String~
    }

    %% Value Objects / Records (document package)
    class PDFDocument {
        <<record>>
        +byte[] data
    }

    class ZPLDocument {
        <<record>>
        +byte[] data
    }

    %% document (labelary sub-package)
    class ZPLLabel {
        <<record>>
        +byte[] data
    }

    %% Domain models (preset.util package)
    class Preset {
        <<record>>
        +String name
        +Map~String, String~ fields
        +getFieldValue(String field) String
        +withFieldValue(String field, String value) Preset
    }

    class PresetFileStore {
        -Path storeFolder
        -Map~String, Map~String, String~~ cache
        +PresetFileStore(String storeFolderPath)
        +load(String presetName) Map~String, String~
        +loadAll() Map~String, Map~String, String~~
        +save(String presetName, Map~String, String~ fields)
        +delete(String presetName)
        -resolveFile(String presetName) Path
        -ensureDirectory()
    }

    %% Abstract base
    class AbstractPresetFieldManager {
        -Map~String, FieldDefinition~ requiredFields
        +getDefaultValueFor(String field) Object
        +getFieldNames() Set~String~
        #getRequiredFieldDefinitions() Map~String, FieldDefinition~
    }

    class FieldDefinition {
        <<record>>
        +Object defaultValue
        +definition(Object defaultValue)$ FieldDefinition
    }

    %% Configuration
    class LabelaryConfig {
        <<record>>
        +String baseUrl
        +String apiKey
    }

    %% Error handling
    class FieldValidationException {
        +FieldValidationException(String message)
        +FieldValidationException(String message, Throwable cause)
    }

    class ConversionException {
        +ConversionException(String message)
        +ConversionException(String message, Throwable cause)
    }

    class PresetManagerException {
        +PresetManagerException(String message)
        +PresetManagerException(String message, Throwable cause)
    }

    class DocumentConverterException {
        +DocumentConverterException(String message)
        +DocumentConverterException(String message, Throwable cause)
    }

    class PresetFileStoreException {
        +PresetFileStoreException(String message)
        +PresetFileStoreException(String message, Throwable cause)
    }

    %% Labelary implementation (labelary package)
    class LabelaryConversionDriver {
        -static final int BATCH_SIZE
        -LabelaryConfig config
        +LabelaryConversionDriver(LabelaryConfig config)
        +requestConversion(ZPLDocument[], Preset) PDFDocument[]*
        -splitIntoBatches(ZPLDocument) ZPLLabel[][]
        -sendBatch(String url, ZPLLabel[]) PDFDocument
    }

    class LabelaryPresetFieldManager {
        -Pattern DPMM_PATTERN$
        -Pattern NUMERIC_PATTERN$
        +validateFor(Map~String, String~ fields)*
    }

    %% Top-level orchestration
    class DocumentConverter {
        -ConversionDriver conversionDriver
        -static final String ZPL_START$
        -static final String ZPL_END$
        +DocumentConverter(ConversionDriver)
        +convert(InputStream[], Preset) PDFDocument[]*
        -parseAndValidate(InputStream) ZPLDocument
        -validateZplContent(byte[] data)
    }

    class PresetManager {
        -Map~String, Preset~ presets
        -PresetFieldManager fieldManager
        -PresetFileStore presetFileStore
        +PresetManager(PresetFieldManager, PresetFileStore)
        +getPreset(String name) Preset
        +createPreset(String name) Preset
        +persistPreset(Preset) Preset
        +deletePreset(String name)
        -loadPresets()
    }

    %% Relationships — implementations
    ConversionDriver <|.. LabelaryConversionDriver : implements
    PresetFieldManager <|.. AbstractPresetFieldManager : implements
    AbstractPresetFieldManager <|-- LabelaryPresetFieldManager : extends
    AbstractPresetFieldManager *-- FieldDefinition : inner record

    %% Relationships — dependencies / usages
    DocumentConverter --> ConversionDriver : uses
    DocumentConverter --> Preset : uses
    DocumentConverter ..> ZPLDocument : creates
    DocumentConverter ..> DocumentConverterException : throws

    PresetManager --> PresetFieldManager : uses
    PresetManager --> PresetFileStore : uses
    PresetManager --> Preset : manages
    PresetManager ..> PresetManagerException : throws
    PresetManager ..> FieldValidationException : throws

    PresetFileStore ..> PresetFileStoreException : throws
    PresetFileStore ..> Preset : loads/saves (fields)

    LabelaryConversionDriver --> LabelaryConfig : uses
    LabelaryConversionDriver ..> ZPLLabel : splits into
    LabelaryConversionDriver ..> PDFDocument : returns
    LabelaryConversionDriver ..> ConversionException : throws

    LabelaryPresetFieldManager ..> FieldValidationException : throws

    %% Inheritance — exceptions
    FieldValidationException --|> Exception : extends
    ConversionException --|> Exception : extends
    PresetManagerException --|> Exception : extends
    DocumentConverterException --|> Exception : extends
    PresetFileStoreException --|> Exception : extends

    %% Legend
    note for PresetFieldManager "methods marked * throw checked exceptions"