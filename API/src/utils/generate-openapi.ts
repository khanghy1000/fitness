import fs from 'fs';
import path from 'path';
import { generateOpenAPIDocument } from '@lib/openapi-registry.ts';

export async function generateApiDocs() {
    try {
        console.log('Generating OpenAPI documentation...');

        const openAPIDocument = generateOpenAPIDocument();

        // Create docs directory if it doesn't exist
        const docsDir = path.join(process.cwd(), 'src/static');
        if (!fs.existsSync(docsDir)) {
            fs.mkdirSync(docsDir, { recursive: true });
        }

        // Write JSON file
        const jsonPath = path.join(docsDir, 'openapi.json');
        fs.writeFileSync(jsonPath, JSON.stringify(openAPIDocument, null, 2));
        console.log(`✅ OpenAPI JSON documentation generated: ${jsonPath}`);
        console.log('\n📚 Documentation generated successfully!');
        console.log('🌐 You can view the documentation at:');
        console.log(
            '   - Swagger UI: https://editor.swagger.io/ (paste the JSON content)'
        );
        console.log(
            '   - Redoc: https://redocly.github.io/redoc/ (paste the JSON content)'
        );
    } catch (error) {
        console.error('❌ Error generating OpenAPI documentation:', error);
        process.exit(1);
    }
}

// generateApiDocs();
