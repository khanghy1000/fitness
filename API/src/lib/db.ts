import 'dotenv/config';
import { drizzle } from 'drizzle-orm/node-postgres';
import * as tables from '../db/schema/tables.ts';
import * as relations from '../db/schema/relations.ts';

const schema = { ...tables, ...relations };

export const db = drizzle(process.env.DATABASE_URL!, { schema });
