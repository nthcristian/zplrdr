# ZPL-RDR – UML Class Diagram

```mermaid
classDiagram
    direction TB

    %% Interfaces (contract package)
    class ConversionDriver {
        <<interface>>
        +requestConversion(ZPLDocument[], Preset) PDFDocument[]
    }

    class PresetFieldManager {
        <<interface>>
        +validateFor(Map~String, String~ fields)
        +getDefaultValueFor(String field) Object
    }

    %% Value Objects / Records (document package)
    class PDFDocument {
        <<record>>
        +String data
    }

    class ZPLDocument {
        <<record>>
        +String data
    }

    class ZPLLabel {
        <<record>>
        +String data
    }

    %% Domain models (preset.util package)
    class Preset {
        <<record>>
        +String name
        +Map~String, String~ fields
        +getFieldValue(String field) String
        +setFieldValue(String field, String value) Preset
    }

    class PresetParser {
        -String presetFolderPath
        +PresetParser(String presetFolderPath)
        +getFieldValue(String presetName, String field) String
        +setFieldValue(String presetName, String field, String value)
        +getMapped(String presetName) Map~String, String~
    }

    %% Error handling
    class FieldValidationException {
        +FieldValidationException()
    }

    %% Labelary implementation (labelary package)
    class LabelaryConversionDriver {
        -static final int BATCH_SIZE
        +requestConversion(ZPLDocument[], Preset) PDFDocument[]
        -splitIntoBatches(ZPLDocument) ZPLLabel[]
        -sendBatch(ZPLLabel[])
    }

    class LabelaryPresetFieldManager {
        -Map~String, FieldDefinition~ fields
        +getDefaultValueFor(String field) Object
        +validateFor(Map~String, String~ fields)
    }

    class FieldDefinition {
        <<record, inner>>
        +Object defaultValue
        +Boolean optional
    }

    %% Top-level orchestration
    class DocumentConverter {
        -ConversionDriver conversionDriver
        +DocumentConverter(ConversionDriver)
        +convert(InputStream[], Preset) PDFDocument[]
        -validateFiles(String[]) ZPLDocument[]
    }

    class PresetManager {
        -Map~String, Preset~ presets
        -PresetFieldManager fieldManager
        -PresetParser presetParser
        +PresetManager(PresetFieldManager, PresetParser)
        +getPreset(String name) Preset
        +setPreset(Preset) Preset
        +deletePreset(String name)
        -loadPresets()
    }

    %% Relationships
    ConversionDriver <|.. LabelaryConversionDriver : implements
    PresetFieldManager <|.. LabelaryPresetFieldManager : implements
    LabelaryPresetFieldManager *-- FieldDefinition : inner record

    DocumentConverter --> ConversionDriver : uses
    DocumentConverter --> Preset : uses
    DocumentConverter ..> ZPLDocument : creates

    PresetManager --> PresetFieldManager : uses
    PresetManager --> PresetParser : uses
    PresetManager --> Preset : manages

    PresetParser ..> FieldValidationException : throws
    PresetParser ..> Preset : reads/writes

    LabelaryConversionDriver ..> ZPLLabel : splits into
    LabelaryConversionDriver ..> PDFDocument : returns

    FieldValidationException --|> Exception : extends