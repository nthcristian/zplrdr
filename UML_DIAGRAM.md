# ZPL-RDR – UML Class Diagram

```mermaid
classDiagram
    direction TB

    %% Interfaces (contract package)
    class ConversionDriver {
        <<interface>>
        +requestConversion(ZPLDocument[], MappedPreset) PDFDocument[]
    }

    class PresetFieldManager {
        <<interface>>
        +getFields() Map~String, Boolean~
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
        +Map~String, String~ fieldValues
        +toMappedPreset() MappedPreset
    }

    class MappedPreset {
        -Map~String, String~ map
        +MappedPreset(Map~String, String~ fields)
        +getFieldValue(String key) String
        +getFields() String[]
    }

    class PresetParser {
        -String presetFolderPath
        +PresetParser(String presetFolderPath)
        +getFieldValue(String presetName, String field) String
        +setFieldValue(String presetName, String field, String value)
        +getFields(String presetName) String[]
    }

    %% Labelary implementation (labelary package)
    class LabelaryConversionDriver {
        -static final int BATCH_SIZE
        +requestConversion(ZPLDocument[], MappedPreset) PDFDocument[]
        -splitIntoBatches(ZPLDocument) ZPLLabel[]
        -sendBatch(ZPLLabel[])
    }

    class LabelaryPresetFieldManager {
        -Map~String, FieldDefinition~ fields
        +getFields() Map~String, Boolean~
        +getDefaultValueFor(String field) Object
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
        +convert(File[], MappedPreset) PDFDocument[]
        -validateFiles(String[]) ZPLDocument[]
    }

    class PresetManager {
        -Map~String, Preset~ presets
        -PresetFieldManager fieldManager
        -PresetParser presetParser
        +PresetManager(PresetFieldManager, PresetParser)
        +getPreset(String name) Preset
        +createPreset(String name) Preset
        +deletePreset(String name)
        -loadPresets()
    }

    %% Relationships
    ConversionDriver <|.. LabelaryConversionDriver : implements
    PresetFieldManager <|.. LabelaryPresetFieldManager : implements
    LabelaryPresetFieldManager *-- FieldDefinition : inner record

    DocumentConverter --> ConversionDriver : uses
    DocumentConverter --> MappedPreset : uses
    DocumentConverter ..> ZPLDocument : creates

    PresetManager --> PresetFieldManager : uses
    PresetManager --> PresetParser : uses
    PresetManager --> Preset : manages

    Preset ..> MappedPreset : produces
    PresetParser ..> Preset : reads/writes

    LabelaryConversionDriver ..> ZPLLabel : splits into
    LabelaryConversionDriver ..> PDFDocument : returns